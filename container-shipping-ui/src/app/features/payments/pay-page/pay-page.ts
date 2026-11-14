import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, input, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { catchError, filter, of, switchMap, take, timer } from 'rxjs';
import { Auth } from '../../../core/auth/auth';
import { BookingApi } from '../../booking/booking.api';
import { PaymentApi } from '../payment.api';
import { Payment } from '../payment.models';
import { RazorpayLoader } from '../razorpay-loader';

type Phase = 'loading' | 'ready' | 'processing' | 'confirming' | 'done' | 'error';

@Component({
  selector: 'app-pay-page',
  imports: [RouterLink, CurrencyPipe, MatButtonModule, MatCardModule, MatIconModule,
            MatProgressBarModule, MatProgressSpinnerModule],
  templateUrl: './pay-page.html',
  styleUrl: './pay-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PayPage implements OnInit {
  readonly id = input.required<string>();   // route param /payments/:id/pay

  private readonly api = inject(PaymentApi);
  private readonly bookings = inject(BookingApi);
  private readonly loader = inject(RazorpayLoader);
  private readonly router = inject(Router);
  private readonly user = inject(Auth).user;

  protected readonly payment = signal<Payment | null>(null);
  protected readonly phase = signal<Phase>('loading');
  protected readonly message = signal<string | null>(null);

  ngOnInit(): void {
    this.api.get(Number(this.id())).subscribe({
      next: (p) => {
        this.payment.set(p);
        if (p.paymentStatus === 'PENDING' || p.paymentStatus === 'FAILED') this.phase.set('ready');
        else { this.phase.set('error'); this.message.set(`This payment is already ${p.paymentStatus.toLowerCase()}.`); }
      },
      error: () => { this.phase.set('error'); this.message.set('Payment not found.'); },
    });
  }

  protected pay(): void {
    const p = this.payment();
    if (!p || this.phase() === 'processing') return;
    this.phase.set('processing');
    this.message.set(null);

    this.loader.load()
      .then(() => this.api.createOrder(p.id).subscribe({
        next: (order) => this.openCheckout(p, order),
        error: (e: HttpErrorResponse) => this.fail(e.error?.message ?? 'Could not start the payment.'),
      }))
      .catch(() => this.fail('Could not load Razorpay. Check your connection.'));
  }

  private openCheckout(p: Payment, order: import('../payment.models').CreateOrderResponse): void {
    const u = this.user();
    const rzp = new window.Razorpay({
      key: order.keyId,
      amount: order.amount,
      currency: order.currency,
      order_id: order.orderId,
      name: 'Container Shipping',
      description: `Booking #${order.bookingId}`,
      prefill: { name: u?.name, email: u?.email, contact: u?.phone },
      theme: { color: '#1976d2' },
      handler: (res: { razorpay_order_id: string; razorpay_payment_id: string; razorpay_signature: string }) =>
        this.verify(p.id, res),
      modal: { ondismiss: () => this.phase() === 'processing' && this.phase.set('ready') },
    });
    rzp.on('payment.failed', (res: any) => {
      const reason: string = res?.error?.description || 'Payment failed';
      this.api.fail(p.id, {
        razorpayOrderId: order.orderId,
        razorpayPaymentId: res?.error?.metadata?.payment_id,
        reason,
      }).subscribe({
        next: (updated) => { this.payment.set(updated); this.fail(`${reason}. You can try again.`); },
        error: () => this.fail(`${reason}. You can try again.`),
      });
    });
    rzp.open();
  }

  private verify(id: number, res: { razorpay_order_id: string; razorpay_payment_id: string; razorpay_signature: string }): void {
    this.api.verify(id, {
      razorpayOrderId: res.razorpay_order_id,
      razorpayPaymentId: res.razorpay_payment_id,
      razorpaySignature: res.razorpay_signature,
    }).subscribe({
      next: (paid) => { this.payment.set(paid); this.awaitConfirmation(paid.bookingId); },
      error: (e: HttpErrorResponse) =>
        this.fail(e.status === 400 ? 'Payment could not be verified. You were not charged by us.'
                                   : (e.error?.message ?? 'Verification failed.')),
    });
  }

  /** booking-service confirms via Kafka: poll up to ~12s, then go to the booking either way */
  private awaitConfirmation(bookingId: number): void {
    this.phase.set('confirming');
    timer(800, 1500).pipe(
      take(8),
      switchMap(() => this.bookings.get(bookingId).pipe(catchError(() => of(null)))),
      filter((b) => !!b && b.status !== 'PENDING'),
      take(1),
    ).subscribe({ complete: () => this.finish(bookingId), next: () => this.finish(bookingId) });

    timer(12_500).pipe(take(1)).subscribe(() => this.finish(bookingId));
  }

  private finished = false;
  private finish(bookingId: number): void {
    if (this.finished) return;
    this.finished = true;
    this.phase.set('done');
    setTimeout(() => this.router.navigate(['/bookings', bookingId]), 1200);
  }

  private fail(msg: string): void {
    this.message.set(msg);
    this.phase.set('ready');   // allow a retry
  }
}