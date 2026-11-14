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
  styles: [`table{width:100%;border-collapse:collapse}th,td{text-align:left;padding:8px;border-bottom:1px solid #eee;vertical-align:middle}
    .error{color:#b00020}.ok{color:#2e7d32}.muted{color:#777}
    .edit{display:flex;gap:8px;align-items:center;flex-wrap:wrap}.edit input,.edit select{padding:6px}`],
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