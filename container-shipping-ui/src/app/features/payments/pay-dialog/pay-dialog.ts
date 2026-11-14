
import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import {
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { forkJoin, map, timer } from 'rxjs';

import { PaymentApi } from '../payment.api';
import {
  Payment,
  PaymentMethod,
  PAYMENT_METHODS,
} from '../payment.models';


@Component({
  selector: 'app-pay-dialog',
  imports: [
    ReactiveFormsModule, CurrencyPipe, MatDialogModule, MatButtonModule, MatRadioModule,
    MatIconModule, MatProgressSpinnerModule,
  ],
  template: `
    <h2 mat-dialog-title>Pay for booking #{{ payment.bookingId }}</h2>
    <mat-dialog-content>
      <p class="amount">{{ payment.amount | currency: payment.currency }}</p>

      <mat-radio-group [formControl]="method" class="methods" aria-label="Payment method">
        @for (m of methods; track m.value) {
          <mat-radio-button [value]="m.value" [disabled]="paying()">
            <mat-icon inline>{{ m.icon }}</mat-icon> {{ m.label }}
          </mat-radio-button>
        }
      </mat-radio-group>

      <small>Demo checkout: no real money moves and no card details are collected.</small>
      @if (error(); as e) { <p class="error">{{ e }}</p> }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null" [disabled]="paying()">Cancel</button>
      <button mat-flat-button (click)="pay()" [disabled]="paying() || method.invalid">
        @if (paying()) { <mat-spinner diameter="20" /> } @else { Pay {{ payment.amount | currency: payment.currency }} }
      </button>
    </mat-dialog-actions>
  `,
  styles: `
    .amount { font-size: 28px; font-weight: 600; margin: 0 0 12px; }
    .methods { display: flex; flex-direction: column; margin-bottom: 8px; }
    .error { color: var(--mat-sys-error); }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayDialog {
  private readonly api = inject(PaymentApi);
  private readonly ref = inject<MatDialogRef<PayDialog, Payment | null>>(MatDialogRef);
  protected readonly payment = inject<Payment>(MAT_DIALOG_DATA);

  protected readonly methods = PAYMENT_METHODS;
  protected readonly method = new FormControl<PaymentMethod>('UPI', { nonNullable: true, validators: Validators.required });
  protected readonly paying = signal(false);
  protected readonly error = signal<string | null>(null);

  protected pay(): void {
    if (this.paying()) return;
    this.paying.set(true);
    this.error.set(null);
    this.ref.disableClose = true;

    forkJoin([this.api.process(this.payment.id, this.method.value), timer(1200)])
      .pipe(map(([p]) => p))
      .subscribe({
        next: (p) => this.ref.close(p),
        error: (err: HttpErrorResponse) => {
          this.paying.set(false);
          this.ref.disableClose = false;
          this.error.set(
            err.status === 0 ? 'Cannot reach the server.'
            : err.status === 409 ? 'This payment was already processed. Close and refresh.'
            : err.status === 404 ? 'Payment not found.'
            : (err.error?.message ?? 'Payment failed. Please try again.'),
          );
        },
      });
  }
}