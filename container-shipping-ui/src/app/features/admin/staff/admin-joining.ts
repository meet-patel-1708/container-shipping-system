import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { CareersService, JobApp } from '../../../core/careers/careers.service';
import { FleetLookup } from '../../fleet/fleet-lookup';

@Component({
  selector: 'app-admin-joining',
  imports: [DatePipe, FormsModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
/* =========================================
   ADMIN JOINING - ONBOARDING DASHBOARD
========================================= */

:host {
  display: block;
  margin-bottom: 30px;

  --primary: #4f46e5;
  --accent: #06b6d4;
  --text: #172033;
  --muted: #64748b;
  --border: #e8edf5;
}

/* SECTION HEADING */
h3 {
  font-size: 19px;
  font-weight: 800;
  color: #1e293b;
  margin: 25px 0 18px;
  letter-spacing: -0.4px;
}

/* TABLE */
table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: 15px;
  overflow: hidden;

  box-shadow: 0 6px 22px rgba(15, 23, 42, 0.035);
  animation: joiningEnter 0.5s ease both;
}

thead {
  background: linear-gradient(120deg, #f8faff, #f1f5ff);
}

th {
  padding: 16px 15px;
  text-align: left;
  color: #64748b;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.7px;
  text-transform: uppercase;
  white-space: nowrap;
}

td {
  padding: 16px 15px;
  color: #334155;
  font-size: 12px;
  border-bottom: 1px solid #edf1f7;
  vertical-align: middle;
  transition: background 0.2s ease;
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

/* EMPLOYEE NAME */
td:first-child {
  color: #1e293b;
  font-weight: 750;
}

/* SECONDARY DETAILS */
td small {
  display: inline-block;
  margin-top: 5px;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 450;
  line-height: 1.6;
}

/* MUTED TEXT */
.muted {
  color: #94a3b8;
  font-size: 11px;
  font-style: italic;
}

/* EDIT FORM */
.edit {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  min-width: 300px;
}

/* INPUTS AND SELECT */
.edit input,
.edit select {
  height: 38px;
  padding: 0 10px;
  border: 1px solid #dbe3ef;
  border-radius: 8px;
  background: #f8fafc;
  color: #334155;
  font-size: 12px;
  outline: none;
  transition: all 0.25s ease;
}

.edit select {
  min-width: 130px;
}

.edit input:focus,
.edit select:focus {
  border-color: var(--primary);
  background: white;
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
}

/* BUTTONS */
button[mat-flat-button] {
  min-height: 37px;
  border-radius: 8px !important;
  background: linear-gradient(135deg, #4f46e5, #6366f1) !important;
  color: white !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  box-shadow: 0 4px 10px rgba(79, 70, 229, 0.17);
  transition: all 0.25s ease !important;
}

button[mat-flat-button]:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 7px 16px rgba(79, 70, 229, 0.25);
}

button[mat-button] {
  min-height: 36px;
  border-radius: 8px !important;
  background: #f1f5f9 !important;
  color: #475569 !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  transition: all 0.25s ease !important;
}

button[mat-button]:hover {
  background: #e0e7ff !important;
  color: #4338ca !important;
}

/* DISABLED */
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ERROR */
.error {
  padding: 12px 15px;
  border-radius: 9px;
  background: #fef2f2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  font-size: 12px;
  font-weight: 600;
  animation: joiningEnter 0.3s ease;
}

/* SUCCESS */
.ok {
  padding: 12px 15px;
  border-radius: 9px;
  background: #ecfdf5;
  color: #047857;
  border: 1px solid #a7f3d0;
  font-size: 12px;
  font-weight: 600;
  animation: joiningEnter 0.3s ease;
}

/* ANIMATION */
@keyframes joiningEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* MOBILE */
@media (max-width: 900px) {
  :host {
    overflow-x: auto;
  }

  table {
    min-width: 850px;
  }
}

@media (max-width: 600px) {
  h3 {
    font-size: 17px;
  }

  th, td {
    padding: 12px;
  }

  .edit {
    min-width: 300px;
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
    <h3>Joining dates</h3>
    @if (error(); as m) { <p class="error">{{ m }}</p> }
    @if (info(); as m) { <p class="ok">{{ m }}</p> }

    <table>
      <thead>
        <tr><th>Employee</th><th>Position</th><th>Onboarding port</th><th>Port ID</th><th>Joining date</th><th>Action</th></tr>
      </thead>
      <tbody>
        @for (a of hired(); track a.applicationId) {
          <tr>
            <td>{{ a.userName }}<br /><small>{{ a.userEmail }} · {{ a.userPhone }}</small></td>
            <td>{{ a.jobTitle }}<br /><small>{{ a.posting }}</small></td>
            <td>{{ a.onboardingPortId ? fleet.portName(a.onboardingPortId) : '-' }}</td>
            <td>{{ a.onboardingPortId ?? '-' }}</td>
            <td>
              @if (a.joiningDate) { {{ a.joiningDate | date: 'mediumDate' }} }
              @else { <span class="muted">Not assigned</span> }
            </td>
            <td>
              @if (editingId() === a.applicationId) {
                <div class="edit">
                  <select [(ngModel)]="portValue">
                    <option [ngValue]="null" disabled>
                      {{ fleet.ports().length ? 'Select port' : 'No ports loaded' }}
                    </option>
                    @for (p of fleet.ports(); track p.portId) { <option [ngValue]="p.portId">{{ p.portName }} (ID {{ p.portId }})</option> }
                  </select>
                  <input type="date" [min]="today" [(ngModel)]="dateValue" />
                  <button mat-flat-button type="button" [disabled]="saving()" (click)="save(a)">Assign &amp; send email</button>
                  <button mat-button type="button" (click)="cancel()">Cancel</button>
                </div>
              } @else {
                <button mat-flat-button type="button" (click)="start(a)">
                  {{ a.joiningDate ? 'Change joining date' : 'Give joining date' }}
                </button>
              }
            </td>
          </tr>
        } @empty { <tr><td colspan="6">No hired employees yet.</td></tr> }
      </tbody>
    </table>`,
})
export class AdminJoining {
  private readonly api = inject(CareersService);
  protected readonly fleet = inject(FleetLookup);
  protected readonly hired = signal<JobApp[]>([]);
  protected readonly editingId = signal<number | null>(null);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);
  protected dateValue = '';
  protected portValue: number | null = null;
  protected readonly today = AdminJoining.localToday();

  constructor() { this.fleet.load(); this.reload(); }

  private static localToday(): string {
    const d = new Date();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${m}-${day}`;
  }

  private reload(): void {
    this.api.applications('HIRED').subscribe((p) => this.hired.set(p.content));
  }

  protected start(a: JobApp): void {
    this.error.set(null); this.info.set(null);
    this.dateValue = a.joiningDate ?? '';
    this.portValue = a.onboardingPortId;
    this.editingId.set(a.applicationId);
  }

  protected cancel(): void { this.editingId.set(null); this.dateValue = ''; this.portValue = null; }

  protected save(a: JobApp): void {
    this.error.set(null); this.info.set(null);
    if (!this.dateValue) { this.error.set('Pick a joining date.'); return; }
    if (this.dateValue < this.today) { this.error.set('Joining date cannot be in the past.'); return; }
    if (this.portValue == null) { this.error.set('Choose the onboarding port.'); return; }
    const portName = this.fleet.portName(this.portValue);
    this.saving.set(true);
    this.api.setJoiningDate(a.applicationId, this.dateValue, this.portValue, portName).subscribe({
      next: () => {
        this.saving.set(false);
        this.info.set(`Joining date set for ${a.userName}. Email sent.`);
        this.cancel();
        this.reload();
      },
      error: (e: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(e.status === 0 ? 'Cannot reach the server.' : e.error?.message ?? 'Something went wrong.');
      },
    });
  }
}
