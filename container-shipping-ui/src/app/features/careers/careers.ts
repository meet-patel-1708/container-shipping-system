import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { CareersService, Job, JobApp } from '../../core/careers/careers.service';

@Component({
  selector: 'app-careers',
  imports: [DatePipe, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`.page{max-width:800px;margin:24px auto;padding:0 16px}.card{border:1px solid #ddd;border-radius:8px;padding:12px;margin-bottom:12px}
    .error{color:#b00020}.ok{color:#2e7d32}`],
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