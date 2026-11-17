import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { ApplicantStatus, CareersService, Job, JobApp } from '../../../core/careers/careers.service';
import { FleetLookup } from '../../fleet/fleet-lookup';

@Component({
  selector: 'app-admin-hiring',
  imports: [DatePipe, FormsModule, MatButtonModule, MatButtonToggleModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
/* =========================================
   ADMIN HIRING - RECRUITMENT DASHBOARD
========================================= */

:host {
  display: block;
  margin: 35px 0;

  --primary: #4f46e5;
  --accent: #06b6d4;
  --success: #059669;
  --danger: #dc2626;
  --text: #172033;
  --border: #e8edf5;
}

/* HEADINGS */
h3 {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
  margin: 30px 0 18px;
  padding-bottom: 13px;
  border-bottom: 1px solid var(--border);
}

h4 {
  font-size: 14px;
  font-weight: 750;
  color: #334155;
  margin: 23px 0 14px;
}

/* FORM ROW */
.row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 13px;
  padding: 15px;

  background: #fff;
  border: 1px solid var(--border);
  border-radius: 12px;

  box-shadow: 0 4px 15px rgba(15, 23, 42, 0.025);
  transition: border-color 0.25s ease;
}

.row:focus-within {
  border-color: #c7d2fe;
}

/* INPUTS */
.row input,
.row select,
.row textarea {
  box-sizing: border-box;
  min-height: 40px;
  padding: 9px 12px;

  border: 1px solid #dbe3ef;
  border-radius: 8px;
  outline: none;

  background: #f8fafc;
  color: #334155;

  font-family: inherit;
  font-size: 12px;

  transition: all 0.25s ease;
}

.row input {
  flex: 1 1 180px;
}

.row select {
  flex: 0 1 180px;
}

.row textarea {
  width: 100%;
  min-height: 85px;
  resize: vertical;
  line-height: 1.6;
}

.row input:focus,
.row select:focus,
.row textarea:focus {
  background: white;
  border-color: var(--primary);
  box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
}

.row input::placeholder,
.row textarea::placeholder {
  color: #94a3b8;
}

/* FILTER GROUP */
mat-button-toggle-group {
  display: inline-flex;
  flex-wrap: wrap;
  margin: 12px 0 17px;

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

/* TABLES */
table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;

  background: white;
  border: 1px solid var(--border);
  border-radius: 13px;
  overflow: hidden;

  box-shadow: 0 6px 22px rgba(15, 23, 42, 0.035);
  margin-bottom: 25px;

  animation: hiringEnter 0.5s ease both;
}

thead {
  background: linear-gradient(120deg, #f8faff, #f1f5ff);
}

th {
  padding: 15px 13px;
  text-align: left;

  color: #64748b;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.7px;
  text-transform: uppercase;
  white-space: nowrap;
}

td {
  padding: 15px 13px;
  border-bottom: 1px solid #edf1f7;

  color: #334155;
  font-size: 12px;
  vertical-align: middle;
}

tbody tr {
  transition: background 0.25s ease;
}

tbody tr:hover {
  background: #f5f7ff;
}

tbody tr:last-child td {
  border-bottom: none;
}

/* APPLICANT DETAILS */
td small {
  display: inline-block;
  margin-top: 5px;
  color: #94a3b8;
  font-size: 11px;
  line-height: 1.5;
}

td:first-child {
  color: #1e293b;
  font-weight: 700;
}

/* SELECT PORT */
td select {
  min-height: 34px;
  max-width: 140px;
  padding: 5px 9px;

  border: 1px solid #dbe3ef;
  border-radius: 7px;
  background: #f8fafc;

  color: #475569;
  font-size: 11px;
  outline: none;
}

/* BUTTONS */
button[mat-flat-button],
button[mat-button] {
  min-height: 35px;
  margin: 3px;

  border-radius: 8px !important;
  font-size: 10px !important;
  font-weight: 750 !important;
  transition: all 0.25s ease !important;
}

/* PRIMARY */
button[mat-flat-button] {
  background: linear-gradient(135deg, #4f46e5, #6366f1) !important;
  color: white !important;
  box-shadow: 0 3px 10px rgba(79, 70, 229, 0.15);
}

button[mat-flat-button]:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(79, 70, 229, 0.25);
}

/* SECONDARY */
button[mat-button] {
  background: #eef2ff !important;
  color: #4338ca !important;
}

button[mat-button]:hover {
  background: #e0e7ff !important;
}

/* ERROR AND SUCCESS */
.error,
.ok {
  padding: 12px 15px;
  border-radius: 9px;
  font-size: 12px;
  font-weight: 600;
  animation: hiringEnter 0.3s ease;
}

.error {
  color: #b91c1c;
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.ok {
  color: #047857;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
}

/* ANIMATION */
@keyframes hiringEnter {
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

  .row {
    align-items: stretch;
  }
}

/* MOBILE */
@media (max-width: 600px) {
  h3 {
    font-size: 18px;
  }

  .row {
    padding: 12px;
  }

  .row input,
  .row select {
    flex: 1 1 100%;
  }

  .row textarea {
    width: 100%;
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
    <h3>Hiring</h3>
    @if (error(); as m) { <p class="error">{{ m }}</p> }
    @if (info(); as m) { <p class="ok">{{ m }}</p> }

    <h4>Post a job</h4>
    <div class="row">
      <input placeholder="Job title" [(ngModel)]="title" maxlength="100" />
      <select [(ngModel)]="posting">
        @for (p of postings; track p) { <option [value]="p">{{ p }}</option> }
      </select>
      <select [(ngModel)]="jobPort">
        <option [ngValue]="null">Any port</option>
        @for (p of fleet.ports(); track p.portId) { <option [ngValue]="p.portId">{{ p.portName }}</option> }
      </select>
    </div>
    <div class="row">
      <textarea rows="3" cols="60" placeholder="Description" [(ngModel)]="description" maxlength="2000"></textarea>
      <button mat-flat-button type="button" (click)="createJob()">Publish job</button>
    </div>

    <table>
      <thead><tr><th>Job</th><th>Posting</th><th>Status</th><th></th></tr></thead>
      <tbody>
        @for (j of jobs(); track j.jobId) {
          <tr><td>{{ j.title }}</td><td>{{ j.posting }}</td><td>{{ j.status }}</td>
            <td><button mat-button type="button" (click)="toggle(j)">{{ j.status === 'OPEN' ? 'Close' : 'Reopen' }}</button></td></tr>
        }
      </tbody>
    </table>

    <h4>Applications</h4>
    <mat-button-toggle-group [value]="filter()" (change)="setFilter($event.value)">
      @for (f of filters; track f) { <mat-button-toggle [value]="f">{{ f || 'ALL' }}</mat-button-toggle> }
    </mat-button-toggle-group>
    <table>
      <thead><tr><th>Applicant</th><th>Job</th><th>Applied</th><th>Status</th><th>Action</th></tr></thead>
      <tbody>
        @for (a of apps(); track a.applicationId) {
          <tr>
            <td>{{ a.userName }}<br /><small>{{ a.userEmail }} · {{ a.userPhone }}</small></td>
            <td>{{ a.jobTitle }}</td>
            <td>{{ a.createdAt | date: 'mediumDate' }}</td>
            <td>{{ a.status }}</td>
            <td>
              <button mat-button type="button" (click)="view(a)">View resume</button>
              @if (a.status === 'APPLIED') {
                <button mat-flat-button type="button" (click)="interview(a)">Take interview</button>
                <button mat-button type="button" (click)="reject(a)">Reject</button>
              }
              @if (a.status === 'SHORTLISTED') {
                <select #port>
                  @for (p of fleet.ports(); track p.portId) { <option [value]="p.portId">{{ p.portName }}</option> }
                </select>
                <button mat-flat-button type="button" (click)="hire(a, port)">Confirm job</button>
                <button mat-button type="button" (click)="reject(a)">Reject</button>
              }
            </td>
          </tr>
        } @empty { <tr><td colspan="5">No applications.</td></tr> }
      </tbody>
    </table>`,
})
export class AdminHiring {
  private readonly api = inject(CareersService);
  protected readonly fleet = inject(FleetLookup);
  protected readonly postings = ['Port officer', 'Ship operator', 'Dispatcher', 'Cargo handler'];
  protected readonly filters: (ApplicantStatus | '')[] = ['APPLIED', 'SHORTLISTED', 'HIRED', 'REJECTED', ''];
  protected readonly jobs = signal<Job[]>([]);
  protected readonly apps = signal<JobApp[]>([]);
  protected readonly filter = signal<ApplicantStatus | ''>('APPLIED');
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);
  protected title = ''; protected posting = 'Port officer'; protected description = '';
  protected jobPort: number | null = null;

  constructor() { this.fleet.load(); this.reload(); }

  private reload(): void {
    this.api.allJobs().subscribe((j) => this.jobs.set(j));
    this.api.applications(this.filter()).subscribe((p) => this.apps.set(p.content));
  }
  protected setFilter(f: ApplicantStatus | ''): void { this.filter.set(f); this.reload(); }

  protected createJob(): void {
    this.clear();
    if (!this.title.trim() || !this.description.trim()) { this.error.set('Enter a title and a description.'); return; }
    this.api.createJob({ title: this.title, posting: this.posting, description: this.description, portId: this.jobPort })
      .subscribe({
        next: () => { this.title = ''; this.description = ''; this.info.set('Job published.'); this.reload(); },
        error: (e) => this.fail(e),
      });
  }
  protected toggle(j: Job): void {
    this.api.setJobStatus(j.jobId, j.status === 'OPEN' ? 'CLOSED' : 'OPEN').subscribe({ next: () => this.reload(), error: (e) => this.fail(e) });
  }
  protected view(a: JobApp): void {
    const tab = window.open('', '_blank');
    this.api.resume(a.applicationId).subscribe({
      next: (b) => { const u = URL.createObjectURL(b); if (tab) tab.location.href = u; setTimeout(() => URL.revokeObjectURL(u), 60000); },
      error: (e) => { tab?.close(); this.fail(e); },
    });
  }
  protected interview(a: JobApp): void {
    this.clear();
    this.api.interview(a.applicationId).subscribe({
      next: () => { this.info.set(`Interview email sent to ${a.userName}.`); this.reload(); },
      error: (e) => this.fail(e),
    });
  }
  protected hire(a: JobApp, sel: HTMLSelectElement): void {
    this.clear();
    const portId = Number(sel.value);
    const portName = this.fleet.portName(portId);
    this.api.hire(a.applicationId, portId, portName).subscribe({
      next: () => { this.info.set(`${a.userName} is hired for ${portName}. Set a joining date in "Joining dates" to activate the staff account.`); this.reload(); this.reload(); },
      error: (e) => this.fail(e),
    });
  }
  protected reject(a: JobApp): void {
    this.api.reject(a.applicationId).subscribe({ next: () => this.reload(), error: (e) => this.fail(e) });
  }
  private clear(): void { this.error.set(null); this.info.set(null); }
  private fail(e: unknown): void {
    const x = e as HttpErrorResponse;
    this.error.set(x.status === 0 ? 'Cannot reach the server.' : x.error?.message ?? 'Something went wrong.');
  }
}
