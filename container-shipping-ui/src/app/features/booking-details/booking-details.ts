import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, filter, interval, map, of, switchMap } from 'rxjs';
import { CatalogApi } from '../catalog/catalog.api';
import { Product } from '../catalog/catalog.model';
import { ConfirmDialog } from '../../shared/ui/confirm-dialog/confirm-dialog';
import { FleetLookup } from '../fleet/fleet-lookup';
import { PaymentApi } from '../payments/payment.api';
import { Payment } from '../payments/payment.models';
import { BookingApi } from '../booking/booking.api';
import { Booking, STATUS_FLOW, STATUS_LABEL, TimelineEvent } from '../booking/booking.models';

@Component({
  selector: 'app-booking-detail',
  imports: [RouterLink, DatePipe, CurrencyPipe, MatButtonModule, MatIconModule, MatProgressBarModule],
  templateUrl: './booking-details.html',
  styleUrl: './booking-details.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookingDetail {
  readonly id = input.required<string>();

  protected readonly fleet = inject(FleetLookup);
  private readonly api = inject(BookingApi);
  private readonly paymentApi = inject(PaymentApi);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  protected readonly booking = signal<Booking | null>(null);
  protected readonly events = signal<TimelineEvent[]>([]);
  protected readonly payment = signal<Payment | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  private readonly products = toSignal(
    inject(CatalogApi).list({ page: 0, size: 100, sort: 'productName,asc', query: '' })
      .pipe(map((r) => r.content), catchError(() => of([] as Product[]))),
    { initialValue: [] as Product[] },
  );

  protected readonly steps = computed(() => {
    const b = this.booking();
    if (!b || b.status === 'CANCELLED' || b.status === 'REJECTED') return [];
    const cur = STATUS_FLOW.indexOf(b.status);
    const ev = [...this.events()].reverse();
    return STATUS_FLOW.map((s, i) => ({
      status: s,
      label: STATUS_LABEL[s],
      event: ev.find((e) => e.status === s) ?? null,
      state: i < cur || (i === cur && s === 'DELIVERED') ? 'done' : i === cur ? 'current' : 'todo',
    }));
  });

  protected readonly canCancel = computed(() => this.booking()?.status === 'PENDING');
  protected readonly canTrack = computed(() => {
    const s = this.booking()?.status;
    return s === 'SHIPPED' || s === 'IN_TRANSIT';
  });
  protected readonly canPay = computed(() => {
    const s = this.payment()?.paymentStatus;
    return (s === 'PENDING' || s === 'FAILED') && this.booking()?.status === 'PENDING';
  });

  constructor() {
    this.fleet.load();
    effect(() => { this.id(); untracked(() => this.load(true)); });
    // quiet auto-refresh so staff updates and payments appear without reloading
    interval(8000).pipe(takeUntilDestroyed()).subscribe(() => {
      const s = this.booking()?.status;
      if (s && s !== 'DELIVERED' && s !== 'CANCELLED') this.load(false);
    });
  }

  protected productName(id: number): string {
    return this.products().find((p) => p.productId === id)?.productName ?? `Product #${id}`;
  }

  protected refresh(): void { this.load(false); }

  private load(first: boolean): void {
    const id = Number(this.id());
    if (first) this.loading.set(true);
    this.api.get(id).subscribe({
      next: (b) => { this.booking.set(b); this.error.set(null); this.loading.set(false); },
      error: (e: HttpErrorResponse) => {
        if (!first) return;
        this.loading.set(false);
        this.error.set(e.status === 0 ? 'Cannot reach the server.' : 'Booking not found.');
      },
    });
    this.api.timeline(id).pipe(catchError(() => of([] as TimelineEvent[])))
      .subscribe((v) => this.events.set(v));
    this.paymentApi.byBooking(id).pipe(catchError(() => of(null as Payment | null)))
      .subscribe((p) => this.payment.set(p));
  }

  protected cancel(b: Booking): void {
    this.dialog.open(ConfirmDialog, {
      data: {
        title: 'Cancel booking?',
        message: `Booking #${b.bookingId} will be cancelled and its space in the container released.`,
        confirmLabel: 'Cancel booking',
      },
    }).afterClosed().pipe(
      filter(Boolean),
      switchMap(() => this.api.cancel(b.bookingId)),
    ).subscribe({
      next: () => { this.snack.open('Booking cancelled', 'Close', { duration: 3000 }); this.load(false); },
      error: (e: HttpErrorResponse) =>
        this.snack.open(e.error?.message ?? 'Could not cancel.', 'Close', { duration: 4000 }),
    });
  }
}