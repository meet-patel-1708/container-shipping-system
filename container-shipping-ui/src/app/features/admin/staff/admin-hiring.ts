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
  styles: [`table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:6px;border-bottom:1px solid #eee;vertical-align:middle}
    .row{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:8px}.row input,.row select,.row textarea{padding:6px}
    .error{color:#b00020}.ok{color:#2e7d32}`],
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