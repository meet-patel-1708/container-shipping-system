import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { Resignation, ResignationService, ResignationStatus } from '../../../core/resignation/resignation.service';
import { FleetLookup } from '../../fleet/fleet-lookup';

@Component({
  selector: 'app-admin-resignations',
  imports: [DatePipe, MatButtonModule, MatButtonToggleModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:6px;border-bottom:1px solid #eee;vertical-align:middle}
    input{width:70px;padding:6px}.error{color:#b00020}.ok{color:#2e7d32}`],
  template: `
    <h3>Resignations</h3>
    @if (error(); as m) { <p class="error">{{ m }}</p> }
    @if (info(); as m) { <p class="ok">{{ m }}</p> }
    <mat-button-toggle-group [value]="filter()" (change)="setFilter($event.value)">
      @for (f of filters; track f) { <mat-button-toggle [value]="f">{{ f || 'ALL' }}</mat-button-toggle> }
    </mat-button-toggle-group>
    <table>
      <thead><tr><th>Employee</th><th>Reason</th><th>Status</th><th>Last working day</th><th>Notice (days)</th></tr></thead>
      <tbody>
        @for (x of items(); track x.resignationId) {
          <tr>
            <td>{{ x.userName }}<br /><small>{{ x.posting }} · {{ x.assignedPortId ? fleet.portName(x.assignedPortId) : '' }}</small></td>
            <td>{{ x.reason }}<br /><small>{{ x.requestedAt | date: 'mediumDate' }}</small></td>
            <td>{{ x.status }}</td>
            <td>{{ x.lastWorkingDate ? (x.lastWorkingDate | date: 'mediumDate') : '-' }}</td>
            <td>
              @if (x.status === 'REQUESTED' || x.status === 'NOTICE') {
                <input #d type="number" min="0" max="180" [value]="x.noticeDays ?? 30" />
                <button mat-flat-button type="button" (click)="setNotice(x, d)">{{ x.status === 'NOTICE' ? 'Update' : 'Accept' }}</button>
                <button mat-button type="button" (click)="reject(x)">{{ x.status === 'NOTICE' ? 'Cancel resignation' : 'Reject' }}</button>
              }
            </td>
          </tr>
        } @empty { <tr><td colspan="5">No resignations.</td></tr> }
      </tbody>
    </table>`,
})
export class AdminResignations {
  private readonly api = inject(ResignationService);
  protected readonly fleet = inject(FleetLookup);
  protected readonly filters: (ResignationStatus | '')[] = ['REQUESTED', 'NOTICE', 'COMPLETED', 'REJECTED', 'WITHDRAWN', ''];
  protected readonly filter = signal<ResignationStatus | ''>('REQUESTED');
  protected readonly items = signal<Resignation[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);

  constructor() { this.fleet.load(); this.load(); }

  private load(): void { this.api.list(this.filter()).subscribe((p) => this.items.set(p.content)); }
  protected setFilter(f: ResignationStatus | ''): void { this.filter.set(f); this.load(); }

  protected setNotice(x: Resignation, input: HTMLInputElement): void {
    this.clear();
    const days = Number(input.value);
    if (!Number.isInteger(days) || days < 0 || days > 180) { this.error.set('Notice must be 0 to 180 days.'); return; }
    this.api.setNotice(x.resignationId, days).subscribe({
      next: (v) => { this.info.set(`${x.userName}: last working day ${v.lastWorkingDate}. Email sent.`); this.load(); },
      error: (e) => this.fail(e),
    });
  }
  protected reject(x: Resignation): void {
    this.clear();
    this.api.reject(x.resignationId).subscribe({ next: () => this.load(), error: (e) => this.fail(e) });
  }
  private clear(): void { this.error.set(null); this.info.set(null); }
  private fail(e: unknown): void {
    const z = e as HttpErrorResponse;
    this.error.set(z.status === 0 ? 'Cannot reach the server.' : z.error?.message ?? 'Something went wrong.');
  }
}