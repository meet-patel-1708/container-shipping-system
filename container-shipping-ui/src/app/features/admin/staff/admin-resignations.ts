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
  styles: [`
/* =========================================
   ADMIN RESIGNATIONS - HR DASHBOARD
========================================= */

:host {
  display: block;
  margin: 35px 0;

  --primary: #4f46e5;
  --success: #059669;
  --danger: #dc2626;
  --warning: #d97706;
  --border: #e8edf5;
}

/* HEADING */
h3 {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
  margin: 30px 0 18px;
  padding-bottom: 13px;
  border-bottom: 1px solid var(--border);
}

/* FILTER */
mat-button-toggle-group {
  display: inline-flex;
  flex-wrap: wrap;
  margin: 10px 0 18px;

  border: 1px solid #dbe3ef !important;
  border-radius: 10px !important;
  overflow: hidden;
  background: white;
}

mat-button-toggle {
  border: none !important;
  background: white !important;
  color: #64748b !important;
  font-size: 11px;
  font-weight: 650;
  transition: all 0.2s ease;
}

mat-button-toggle:hover {
  background: #eef2ff !important;
}

mat-button-toggle.mat-button-toggle-checked {
  background: linear-gradient(135deg, #4f46e5, #6366f1) !important;
  color: white !important;
}

/* TABLE */
table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;

  background: #fff;
  border: 1px solid var(--border);
  border-radius: 14px;
  overflow: hidden;

  box-shadow: 0 6px 22px rgba(15, 23, 42, 0.035);
  animation: resignationEnter 0.5s ease both;
}

/* HEADER */
thead {
  background: linear-gradient(120deg, #f8faff, #f1f5ff);
}

th {
  padding: 16px 14px;
  text-align: left;

  color: #64748b;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.7px;
  text-transform: uppercase;
  white-space: nowrap;
}

/* CELLS */
td {
  padding: 16px 14px;
  border-bottom: 1px solid #edf1f7;

  color: #334155;
  font-size: 12px;
  vertical-align: middle;
}

tbody tr {
  transition: all 0.25s ease;
}

tbody tr:hover {
  background: #f5f7ff;
}

tbody tr:last-child td {
  border-bottom: none;
}

/* EMPLOYEE */
td:first-child {
  color: #1e293b;
  font-weight: 750;
}

/* REASON AND PORT DETAILS */
td small {
  display: inline-block;
  margin-top: 5px;
  color: #94a3b8;
  font-size: 11px;
  line-height: 1.5;
}

/* NOTICE DAYS INPUT */
td input[type="number"] {
  box-sizing: border-box;
  width: 80px;
  height: 36px;
  padding: 0 9px;

  border: 1px solid #dbe3ef;
  border-radius: 8px;

  background: #f8fafc;
  color: #334155;
  font-size: 12px;
  outline: none;

  transition: all 0.25s ease;
}

td input[type="number"]:focus {
  background: white;
  border-color: var(--primary);
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
}

/* ACCEPT / UPDATE */
button[mat-flat-button] {
  min-height: 36px;
  margin: 3px;
  padding: 0 13px !important;

  border-radius: 8px !important;
  background: linear-gradient(135deg, #059669, #10b981) !important;
  color: white !important;

  font-size: 10px !important;
  font-weight: 750 !important;

  box-shadow: 0 3px 10px rgba(5, 150, 105, 0.15);
  transition: all 0.25s ease !important;
}

button[mat-flat-button]:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(5, 150, 105, 0.25);
}

/* REJECT / CANCEL */
button[mat-button] {
  min-height: 35px;
  margin: 3px;
  padding: 0 12px !important;

  border-radius: 8px !important;
  background: #fff1f2 !important;
  color: #dc2626 !important;

  font-size: 10px !important;
  font-weight: 750 !important;

  transition: all 0.25s ease !important;
}

button[mat-button]:hover {
  background: #ffe4e6 !important;
  transform: translateY(-2px);
}

/* ERROR */
.error {
  padding: 12px 15px;
  border-radius: 9px;

  color: #b91c1c;
  background: #fef2f2;
  border: 1px solid #fecaca;

  font-size: 12px;
  font-weight: 600;
  animation: resignationEnter 0.3s ease;
}

/* SUCCESS */
.ok {
  padding: 12px 15px;
  border-radius: 9px;

  color: #047857;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;

  font-size: 12px;
  font-weight: 600;
  animation: resignationEnter 0.3s ease;
}

/* EMPTY TABLE */
td[colspan="5"] {
  text-align: center;
  padding: 30px;
  color: #94a3b8;
  font-size: 13px;
}

/* ANIMATION */
@keyframes resignationEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* TABLET */
@media (max-width: 900px) {
  :host {
    overflow-x: auto;
  }

  table {
    min-width: 750px;
  }
}

/* MOBILE */
@media (max-width: 600px) {
  h3 {
    font-size: 18px;
  }

  th, td {
    padding: 12px;
  }

  mat-button-toggle-group {
    max-width: 100%;
    overflow-x: auto;
  }
}

@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
`],
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
