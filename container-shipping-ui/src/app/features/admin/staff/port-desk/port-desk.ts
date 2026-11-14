import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { Auth } from '../../../../core/auth/auth';
import { StaffApi } from '../../../../core/staff/staff-api';
import { Booking } from '../../../booking/booking.models';
import { FleetLookup } from '../../../fleet/fleet-lookup';

@Component({
  selector: 'app-port-desk',
  imports: [DatePipe, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`.page{max-width:1000px;margin:24px auto;padding:0 16px}.card{border:1px solid #ddd;border-radius:8px;padding:12px;margin-bottom:12px}
    .row{display:flex;gap:10px;align-items:center;flex-wrap:wrap;margin-top:8px}input[type=text],input[type=number]{padding:6px}
    .error{color:#b00020}.ok{color:#2e7d32}`],
  template: `
    <div class="page">
      <h2>Port desk</h2>
      @if (error(); as m) { <p class="error">{{ m }}</p> }
      @if (info(); as m) { <p class="ok">{{ m }}</p> }

      <h3>Goods arriving at the port</h3>
      <p><small>Send the customer an SMS code, inspect the goods, then accept with the code or reject with a reason.</small></p>
      @for (b of incoming(); track b.bookingId) {
        <div class="card">
          <strong>Booking #{{ b.bookingId }}</strong> · container #{{ b.containerId }} ·
          {{ fleet.portName(b.originPortId) }} → {{ fleet.portName(b.destPortId) }}<br />
          <small>Departs {{ b.scheduleAt | date: 'medium' }} · declared weight {{ b.totalWeightKg }} kg</small>
          <ul>@for (i of b.items; track i.itemId) { <li>{{ i.productName ?? ('Product #' + i.productId) }} × {{ i.quantity }}</li> }</ul>
          @if (due(b)) {
            <div class="row">
              <button mat-stroked-button type="button" (click)="sendCode(b, false)">Send code to customer (SMS)</button>
            </div>
            <div class="row">
              <label><input type="checkbox" #pk checked /> Packaging valid</label>
              <label><input type="checkbox" #lb checked /> Label and documents valid</label>
              <label>Actual weight (kg) <input type="number" #wt [value]="b.totalWeightKg" step="0.01" min="0.01" /></label>
            </div>
            <div class="row">
              <input type="text" #cd maxlength="6" placeholder="6-digit code" />
              <input type="text" #nt placeholder="Note, or the rejection reason" size="32" />
              <button mat-flat-button type="button" (click)="accept(b, pk.checked, lb.checked, wt.value, cd.value, nt.value)">Accept goods</button>
              <button mat-stroked-button type="button" (click)="reject(b, pk.checked, lb.checked, wt.value, nt.value)">Reject goods</button>
            </div>
          } @else {
            <small>Goods are received from {{ b.scheduleAt | date: 'mediumDate' }}.</small>
          }
        </div>
      } @empty { <p>No goods are waiting for this port.</p> }

      <h3>Deliveries at this port</h3>
      <p><small>The container has arrived. Send the customer a code and enter it when they collect their goods.</small></p>
      @for (b of deliveries(); track b.bookingId) {
        <div class="card">
          <strong>Booking #{{ b.bookingId }}</strong> · container #{{ b.containerId }} · {{ b.totalWeightKg }} kg
          <ul>@for (i of b.items; track i.itemId) { <li>{{ i.productName ?? ('Product #' + i.productId) }} × {{ i.quantity }}</li> }</ul>
          <div class="row">
            <button mat-stroked-button type="button" (click)="sendCode(b, true)">Send code to customer (SMS)</button>
            <input type="text" #dc maxlength="6" placeholder="6-digit code" />
            <button mat-flat-button type="button" (click)="deliver(b, dc.value)">Hand over goods</button>
          </div>
        </div>
      } @empty { <p>No deliveries waiting.</p> }
      <button mat-button type="button" (click)="load()">Refresh</button>
    </div>`,
})
export class PortDesk {
  private readonly api = inject(StaffApi);
  private readonly auth = inject(Auth);
  protected readonly fleet = inject(FleetLookup);
  protected readonly all = signal<Booking[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);
  private readonly mine = computed(() => this.auth.user()?.assignedPortId ?? null);

  private ok(port: number): boolean { return this.auth.isAdmin() || this.mine() === port; }
  protected readonly incoming = computed(() => this.all()
    .filter((b) => b.status === 'CONFIRMED' && this.ok(b.originPortId))
    .sort((a, b) => a.scheduleAt.localeCompare(b.scheduleAt)));
  protected readonly deliveries = computed(() => this.all().filter((b) => b.status === 'ARRIVED' && this.ok(b.destPortId)));

  constructor() { this.fleet.load(); this.load(); }

  protected load(): void {
    this.api.bookings().subscribe({ next: (b) => this.all.set(b), error: (e) => this.fail(e) });
  }
  protected due(b: Booking): boolean {
    const d = new Date(); const t = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    return b.scheduleAt.slice(0, 10) <= t;
  }
  protected sendCode(b: Booking, delivery: boolean): void {
    this.clear();
    this.api.sendCode(b.bookingId, delivery).subscribe({
      next: () => this.info.set(`Code sent to the customer of booking #${b.bookingId}.`), error: (e) => this.fail(e) });
  }
  protected accept(b: Booking, pk: boolean, lb: boolean, wt: string, otp: string, note: string): void {
    this.clear();
    if (!/^\d{6}$/.test(otp.trim())) { this.error.set('Enter the 6-digit code.'); return; }
    this.api.receive(b.bookingId, { otp: otp.trim(), packagingOk: pk, labelOk: lb, actualWeightKg: Number(wt), note }).subscribe({
      next: () => { this.info.set(`Booking #${b.bookingId} received at the port.`); this.load(); }, error: (e) => this.fail(e) });
  }
  protected reject(b: Booking, pk: boolean, lb: boolean, wt: string, reason: string): void {
    this.clear();
    if (!reason.trim()) { this.error.set('Write the rejection reason in the note box.'); return; }
    if (!confirm(`Reject booking #${b.bookingId}? The customer is notified and the payment is marked refunded.`)) return;
    this.api.rejectGoods(b.bookingId, { packagingOk: pk, labelOk: lb, actualWeightKg: Number(wt), reason: reason.trim() }).subscribe({
      next: () => { this.info.set(`Booking #${b.bookingId} rejected.`); this.load(); }, error: (e) => this.fail(e) });
  }
  protected deliver(b: Booking, otp: string): void {
    this.clear();
    if (!/^\d{6}$/.test(otp.trim())) { this.error.set('Enter the 6-digit code.'); return; }
    this.api.deliver(b.bookingId, otp.trim()).subscribe({
      next: () => { this.info.set(`Booking #${b.bookingId} delivered.`); this.load(); }, error: (e) => this.fail(e) });
  }
  private clear(): void { this.error.set(null); this.info.set(null); }
  private fail(e: unknown): void {
    const x = e as HttpErrorResponse;
    this.error.set(x.status === 0 ? 'Cannot reach the server.' : x.error?.message ?? 'Something went wrong.');
  }
}