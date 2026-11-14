import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { Resignation, ResignationService } from '../../core/resignation/resignation.service';

@Component({
  selector: 'app-resignation',
  imports: [DatePipe, FormsModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`.page{max-width:700px;margin:24px auto;padding:0 16px}.box{border:1px solid #ddd;border-radius:8px;padding:12px;margin:12px 0}
    textarea{width:100%;padding:8px}.error{color:#b00020}.ok{color:#2e7d32}`],
  template: `
    <div class="page">
      <h2>Resignation</h2>
      @if (error(); as m) { <p class="error">{{ m }}</p> }
      @if (info(); as m) { <p class="ok">{{ m }}</p> }

      @if (r(); as x) {
        @if (x.status === 'REQUESTED') {
          <div class="box">
            <strong>Request sent</strong> on {{ x.requestedAt | date: 'mediumDate' }}.<br />
            Waiting for an admin to set your notice period.
            <p><small>Reason: {{ x.reason }}</small></p>
            <button mat-stroked-button type="button" (click)="withdraw()">Withdraw request</button>
          </div>
        } @else if (x.status === 'NOTICE') {
          <div class="box">
            <strong>Notice period: {{ x.noticeDays }} day(s)</strong><br />
            Your last working day is <strong>{{ x.lastWorkingDate | date: 'fullDate' }}</strong>.
            You keep full access until the end of that day.
            <p><small>The admin can change the notice period if needed.</small></p>
          </div>
        } @else { <p>Your last resignation is {{ x.status.toLowerCase() }}.</p> }
      }

      @if (!open()) {
        <div class="box">
          <p>If you want to resign, tell us why. An admin will set your notice period.</p>
          <textarea rows="4" maxlength="500" [(ngModel)]="reason" placeholder="Reason"></textarea>
          <button mat-flat-button type="button" [disabled]="busy()" (click)="submit()">Submit resignation</button>
        </div>
      }
    </div>`,
})
export class ResignationPage {
  private readonly api = inject(ResignationService);
  protected readonly r = signal<Resignation | null>(null);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);
  protected reason = '';

  constructor() { this.load(); }

  protected open(): boolean { const s = this.r()?.status; return s === 'REQUESTED' || s === 'NOTICE'; }

  private load(): void { this.api.mine().subscribe((v) => this.r.set(v)); }

  protected submit(): void {
    this.error.set(null); this.info.set(null);
    if (!this.reason.trim()) { this.error.set('Please enter a reason.'); return; }
    this.busy.set(true);
    this.api.request(this.reason).subscribe({
      next: (v) => { this.busy.set(false); this.r.set(v); this.reason = ''; this.info.set('Resignation submitted.'); },
      error: (e) => this.fail(e),
    });
  }
  protected withdraw(): void {
    this.error.set(null);
    this.api.withdraw().subscribe({ next: (v) => { this.r.set(v); this.info.set('Request withdrawn.'); }, error: (e) => this.fail(e) });
  }
  private fail(e: unknown): void {
    this.busy.set(false);
    const x = e as HttpErrorResponse;
    this.error.set(x.status === 0 ? 'Cannot reach the server.' : x.error?.message ?? 'Something went wrong.');
  }
}