import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { StaffApi } from '../staff-api';
import { FleetLookup } from '../../../features/fleet/fleet-lookup';
import { Booking, BookingStatus, STATUS_LABEL } from '../../../features/booking/booking.models';
import { Auth } from '../../auth/auth';

const VOYAGE: BookingStatus[] = ['SHIPPED', 'IN_TRANSIT', 'ARRIVED'];
interface Group {
  containerId: number; bookings: Booking[]; voyage: BookingStatus | null;
  originPortId: number; destPortId: number; capacity: number; remaining: number;
}

@Component({
  selector: 'app-staff-movements',
  imports: [DatePipe, MatButtonModule, MatIconModule, MatProgressBarModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './staff-movement.scss',
  template: `
    <div class="page">
      <h2>Container movements</h2>
      <p><small>Click a container to load it. Tick the received goods to ship; goods not ticked are offloaded and refunded.</small></p>
      @if (loading()) { <mat-progress-bar mode="indeterminate" /> }
      @if (error(); as m) { <p class="error">{{ m }}</p> }
      @if (info(); as m) { <p class="ok">{{ m }}</p> }

      @for (g of groups(); track g.containerId) {
        <div class="box">
          <div class="head" style="cursor:pointer" (click)="toggle(g.containerId)">
            <div>
              <strong>Container #{{ g.containerId }}</strong>
              <small> · {{ fleet.portName(g.originPortId) }} → {{ fleet.portName(g.destPortId) }}
                · {{ g.remaining }} kg free of {{ g.capacity }} kg · {{ g.voyage ? label[g.voyage] : 'Loading' }}</small>
            </div>
            <mat-icon>{{ open() === g.containerId ? 'expand_less' : 'expand_more' }}</mat-icon>
          </div>

          @if (open() === g.containerId) {
            <table>
              <thead><tr><th></th><th>Booking</th><th>Goods</th><th>Weight</th><th>Departs</th><th>Status</th></tr></thead>
              <tbody>
                @for (b of g.bookings; track b.bookingId) {
                  <tr>
                    <td>@if (!g.voyage && b.status === 'AT_PORT') {
                      <input type="checkbox" [checked]="picked().has(b.bookingId)" (change)="pick(b.bookingId)" />
                    }</td>
                    <td>#{{ b.bookingId }}</td>
                    <td>@for (i of b.items; track i.itemId) { {{ i.productName ?? ('Product #' + i.productId) }} × {{ i.quantity }}; }</td>
                    <td>{{ b.totalWeightKg }} kg</td>
                    <td>{{ b.scheduleAt | date: 'medium' }}</td>
                    <td>{{ label[b.status] }}</td>
                  </tr>
                }
              </tbody>
            </table>

            @if (!g.voyage) {
              @if (canOrigin(g)) {
                <p>Selected: {{ chosen(g).length }} booking(s), {{ chosenKg(g) }} kg.
                  @if (chosen(g).length) { Ships from <strong>{{ dueAt(g) | date: 'medium' }}</strong>. }
                  @if (offloaded(g) > 0) { <span class="error">{{ offloaded(g) }} other booking(s) will be offloaded.</span> }</p>
                <button mat-flat-button type="button" [disabled]="busy() === g.containerId || !chosen(g).length || !timeReached(g)"
                        (click)="ship(g)">Ship selected</button>
                @if (chosen(g).length && !timeReached(g)) { <small> Not before {{ dueAt(g) | date: 'medium' }}</small> }
              } @else { <small>Loading is done by {{ fleet.portName(g.originPortId) }} staff.</small> }
            } @else if (g.voyage === 'SHIPPED') {
              @if (canOrigin(g)) { <button mat-flat-button type="button" (click)="advance(g, 'IN_TRANSIT')">Start transit (live GPS)</button> }
            } @else if (g.voyage === 'IN_TRANSIT') {
              @if (canDest(g)) { <button mat-flat-button type="button" (click)="advance(g, 'ARRIVED')">Mark arrived</button> }
              @if (canOrigin(g)) { <button mat-stroked-button type="button" (click)="startSim(g)">Restart GPS</button> }
            } @else { <small>Arrived. Hand over each booking from the Port desk.</small> }
          }
        </div>
      } @empty { @if (!loading()) { <p>No active containers.</p> } }
      <button mat-button type="button" (click)="load()"><mat-icon>refresh</mat-icon> Refresh</button>
    </div>`,
})
export class StaffMovements {
  private readonly api = inject(StaffApi);
  private readonly auth = inject(Auth);
  protected readonly fleet = inject(FleetLookup);
  protected readonly label = STATUS_LABEL;

  protected readonly all = signal<Booking[]>([]);
  protected readonly loading = signal(true);
  protected readonly busy = signal<number | null>(null);
  protected readonly open = signal<number | null>(null);
  protected readonly picked = signal<Set<number>>(new Set());
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);
  private readonly mine = computed(() => this.auth.user()?.assignedPortId ?? null);

  protected readonly groups = computed<Group[]>(() => {
    const map = new Map<number, Booking[]>();
    for (const b of this.all()) {
      if (['DELIVERED', 'CANCELLED', 'REJECTED'].includes(b.status)) continue;
      if (!map.has(b.containerId)) map.set(b.containerId, []);
      map.get(b.containerId)!.push(b);
    }
    return [...map.entries()].map(([containerId, bookings]) => ({
      containerId, bookings,
      voyage: bookings.find((b) => VOYAGE.includes(b.status))?.status ?? null,
      originPortId: bookings[0].originPortId, destPortId: bookings[0].destPortId,
      capacity: bookings[0].containerCapacityKg, remaining: bookings[0].containerRemainingKg,
    } as Group)).sort((a, b) => a.containerId - b.containerId);
  });

  constructor() { this.fleet.load(); this.load(); }

  protected canOrigin(g: Group): boolean { return this.auth.isAdmin() || this.mine() === g.originPortId; }
  protected canDest(g: Group): boolean { return this.auth.isAdmin() || this.mine() === g.destPortId; }
  protected toggle(id: number): void { this.open.update((o) => (o === id ? null : id)); this.picked.set(new Set()); }
  protected pick(id: number): void {
    this.picked.update((s) => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });
  }
  protected chosen(g: Group): Booking[] { return g.bookings.filter((b) => b.status === 'AT_PORT' && this.picked().has(b.bookingId)); }
  protected chosenKg(g: Group): number { return this.chosen(g).reduce((s, b) => s + Number(b.totalWeightKg), 0); }
  protected offloaded(g: Group): number {
    return g.bookings.filter((b) => ['PENDING', 'CONFIRMED', 'AT_PORT'].includes(b.status) && !this.picked().has(b.bookingId)).length;
  }
  protected dueAt(g: Group): string {
    return this.chosen(g).map((b) => b.scheduleAt).sort().at(-1) ?? '';
  }
  protected timeReached(g: Group): boolean { const d = this.dueAt(g); return !!d && new Date(d) <= new Date(); }

  protected load(): void {
    this.api.bookings().subscribe({
      next: (b) => { this.all.set(b); this.loading.set(false); },
      error: (e) => { this.loading.set(false); this.error.set(this.msg(e)); },
    });
  }

  protected ship(g: Group): void {
    this.clear();
    const off = this.offloaded(g);
    if (off > 0 && !confirm(`${off} booking(s) not selected will be cancelled and refunded. Continue?`)) return;
    this.busy.set(g.containerId);
    this.api.ship(g.containerId, this.chosen(g).map((b) => b.bookingId), 'Shipped by port staff').subscribe({
      next: (r) => { this.busy.set(null); this.picked.set(new Set()); this.info.set(`${r.bookingsUpdated} booking(s) shipped.`); this.load(); },
      error: (e) => { this.busy.set(null); this.error.set(this.msg(e)); },
    });
  }

  protected advance(g: Group, target: 'IN_TRANSIT' | 'ARRIVED'): void {
    this.clear();
    this.busy.set(g.containerId);
    this.api.advance(g.containerId, target, target === 'IN_TRANSIT' ? 'Voyage started' : 'Arrived at destination port').subscribe({
      next: (r) => {
        this.busy.set(null);
        this.info.set(`Container #${g.containerId}: ${r.bookingsUpdated} booking(s) → ${STATUS_LABEL[r.status]}.`);
        if (target === 'IN_TRANSIT') this.startSim(g);
        else this.api.stopSimulation(g.containerId).subscribe({ error: () => {} });
        this.load();
      },
      error: (e) => { this.busy.set(null); this.error.set(this.msg(e)); },
    });
  }

  /** no real ship at sea, so a simulated voyage is played between the two ports */
  protected startSim(g: Group): void {
    const o = this.fleet.port(g.originPortId), d = this.fleet.port(g.destPortId);
    if (o?.latitude == null || o.longitude == null || d?.latitude == null || d.longitude == null) {
      this.error.set('Add latitude and longitude to both ports in Fleet to run the simulated voyage.'); return;
    }
    this.api.simulate({ containerId: g.containerId, fromLat: o.latitude, fromLng: o.longitude,
      toLat: d.latitude, toLng: d.longitude, steps: 60, intervalMs: 2000 }).subscribe({
      next: () => this.info.set(`Simulated voyage started for container #${g.containerId}.`),
      error: (e) => this.error.set(this.msg(e)),
    });
  }

  private clear(): void { this.error.set(null); this.info.set(null); }
  private msg(err: unknown): string {
    const e = err as HttpErrorResponse;
    return e.status === 0 ? 'Cannot reach the server.' : e.error?.message ?? 'Something went wrong.';
  }
}