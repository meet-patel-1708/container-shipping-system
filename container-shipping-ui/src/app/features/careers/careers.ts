import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { CareersService, Job, JobApp } from '../../core/careers/careers.service';

@Component({
  selector: 'app-careers',
  imports: [DatePipe, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
:host {
  display: block;
  min-height: 100%;
  font-family: 'Inter', sans-serif;
  color: #172554;
}

.page {
  max-width: 1150px;
  margin: 0 auto;
  padding: 32px;
  animation: pageEnter 0.55s ease both;
}

/* Page headings */
h2 {
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -1px;
  color: #172554;
  margin: 0 0 28px;
}

h3 {
  font-size: 20px;
  font-weight: 750;
  color: #1e3a8a;
  margin: 34px 0 18px;
  display: flex;
  align-items: center;
  gap: 10px;
}

h3::before {
  content: '';
  width: 5px;
  height: 23px;
  border-radius: 10px;
  background: linear-gradient(180deg, #6366f1, #06b6d4);
}

/* Job cards */
.card {
  position: relative;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 18px;
  box-shadow: 0 4px 15px rgba(15, 23, 42, 0.035);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease,
    border-color 0.3s ease;
  animation: cardEnter 0.5s ease both;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 0;
  background: linear-gradient(180deg, #6366f1, #06b6d4);
  transition: height 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
  border-color: #c7d2fe;
  box-shadow: 0 14px 35px rgba(79, 70, 229, 0.10);
}

.card:hover::before {
  height: 100%;
}

/* Job title */
.card > strong {
  display: inline-block;
  font-size: 19px;
  font-weight: 750;
  color: #172554;
  margin-bottom: 8px;
}

/* Job metadata */
.card small {
  display: inline-block;
  color: #64748b;
  font-size: 12px;
  line-height: 1.7;
}

/* Job description */
.card p {
  color: #475569;
  font-size: 14px;
  line-height: 1.8;
  margin: 12px 0 20px;
}

/* Apply button */
.card button[mat-flat-button] {
  min-height: 42px;
  padding: 0 20px;
  border-radius: 9px;
  font-weight: 650;
  letter-spacing: 0.1px;
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #ffffff;
  box-shadow: 0 5px 12px rgba(79, 70, 229, 0.2);
  transition: all 0.25s ease;
}

.card button[mat-flat-button]:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(79, 70, 229, 0.3);
}

.card button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* Resume helper text */
.card button + small {
  margin-left: 10px;
  color: #94a3b8;
}

/* Already applied */
.card > small {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
  border-radius: 20px;
  color: #047857;
  font-weight: 650;
}

/* My applications */
.card > strong + span {
  color: #475569;
}

/* Error and success messages */
.error,
.ok {
  padding: 13px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  margin: 16px 0;
  animation: messageEnter 0.3s ease both;
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

/* Empty states */
.page > p {
  color: #64748b;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  padding: 22px;
  font-size: 14px;
  text-align: center;
}

/* Animations */
@keyframes pageEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes messageEnter {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .page {
    padding: 20px 15px;
  }

  h2 {
    font-size: 25px;
  }

  .card {
    padding: 18px;
  }

  .card > strong {
    font-size: 17px;
  }

  .card button + small {
    display: block;
    margin: 10px 0 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
`],
  template: `
    <div class="page">
      <h2>Careers</h2>
      @if (error(); as m) { <p class="error">{{ m }}</p> }
      @if (info(); as m) { <p class="ok">{{ m }}</p> }

      <h3>Open positions</h3>
      @for (j of jobs(); track j.jobId) {
        <div class="card">
          <strong>{{ j.title }}</strong> <small>· {{ j.posting }} · posted {{ j.createdAt | date: 'mediumDate' }}</small>
          <p>{{ j.description }}</p>
          @if (applied(j.jobId)) { <small>You have applied.</small> }
          @else {
            <input #f type="file" hidden accept="application/pdf,image/jpeg,image/png" (change)="apply(j, $event)" />
            <button mat-flat-button type="button" [disabled]="busy()" (click)="f.click()">Apply with resume</button>
            <small> PDF, JPG or PNG, up to 5 MB</small>
          }
        </div>
      } @empty { <p>No open positions right now.</p> }

      <h3>My applications</h3>
      @for (a of mine(); track a.applicationId) {
        <div class="card">
          <strong>{{ a.jobTitle }}</strong> — {{ label(a.status) }}
        </div>
      } @empty { <p>You have not applied yet.</p> }
    </div>`,
})
export class Careers {
  private readonly api = inject(CareersService);
  protected readonly jobs = signal<Job[]>([]);
  protected readonly mine = signal<JobApp[]>([]);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);

  constructor() { this.load(); }

  private load(): void {
    this.api.jobs().subscribe((j) => this.jobs.set(j));
    this.api.mine().subscribe((m) => this.mine.set(m));
  }
  protected applied(id: number): boolean { return this.mine().some((a) => a.jobId === id); }
  protected label(s: string): string {
    return ({ APPLIED: 'Under review', SHORTLISTED: 'Shortlisted for interview', HIRED: 'Selected', REJECTED: 'Not selected' } as Record<string, string>)[s] ?? s;
  }
  protected apply(j: Job, e: Event): void {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) return;
    if (file.size > 5 * 1024 * 1024) { this.error.set('Resume must be 5 MB or smaller.'); return; }
    this.busy.set(true); this.error.set(null); this.info.set(null);
    this.api.apply(j.jobId, file).subscribe({
      next: () => { this.busy.set(false); this.info.set('Application sent.'); this.load(); },
      error: (e: HttpErrorResponse) => { this.busy.set(false); this.error.set(e.error?.message ?? 'Could not apply.'); },
    });
  }
}
