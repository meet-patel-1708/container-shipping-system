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

styles: [`
  .page {
    max-width: 850px;
    margin: 32px auto;
    padding: 24px;
    color: #172554;
  }

  h2 {
    font-size: 28px;
    font-weight: 750;
    color: #172554;
    margin-bottom: 24px;
    letter-spacing: -0.5px;
  }

  .box {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 14px;
    padding: 24px;
    margin: 18px 0;
    box-shadow: 0 4px 15px rgba(15, 23, 42, 0.04);
    transition: box-shadow 0.2s ease, transform 0.2s ease;
  }

  .box:hover {
    box-shadow: 0 8px 24px rgba(15, 23, 42, 0.07);
  }

  .box strong {
    color: #172554;
    font-weight: 700;
  }

  .box p {
    color: #475569;
    line-height: 1.7;
    margin: 14px 0;
  }

  .box small {
    color: #64748b;
    font-size: 13px;
  }

  textarea {
    display: block;
    width: 100%;
    box-sizing: border-box;
    min-height: 120px;
    padding: 14px;
    margin: 16px 0;
    border: 1px solid #cbd5e1;
    border-radius: 10px;
    background: #f8fafc;
    color: #1e293b;
    font-family: inherit;
    font-size: 14px;
    resize: vertical;
    outline: none;
    transition: border-color 0.2s, box-shadow 0.2s;
  }

  textarea:focus {
    border-color: #6366f1;
    background: #ffffff;
    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
  }

  textarea::placeholder {
    color: #94a3b8;
  }

  button {
    border-radius: 9px !important;
    font-weight: 600 !important;
    padding: 8px 18px !important;
    transition: all 0.2s ease !important;
  }

  .error {
    background: #fef2f2;
    color: #b91c1c;
    border: 1px solid #fecaca;
    border-radius: 9px;
    padding: 12px 16px;
    margin: 14px 0;
    font-size: 14px;
  }

  .ok {
    background: #ecfdf5;
    color: #047857;
    border: 1px solid #a7f3d0;
    border-radius: 9px;
    padding: 12px 16px;
    margin: 14px 0;
    font-size: 14px;
  }

  @media (max-width: 600px) {
    .page {
      padding: 16px;
      margin: 12px auto;
    }

    h2 {
      font-size: 23px;
    }

    .box {
      padding: 18px;
    }

    textarea {
      min-height: 100px;
    }
  }
`],

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
