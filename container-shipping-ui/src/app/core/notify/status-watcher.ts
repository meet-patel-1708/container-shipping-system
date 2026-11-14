import { DestroyRef, inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Auth } from '../auth/auth';
import { StaffApi } from '../staff/staff-api';
import { BookingApi } from '../../features/booking/booking.api';
import { Booking, BookingStatus } from '../../features/booking/booking.models';

const TEXT: Partial<Record<BookingStatus, string>> = {
  SHIPPED: 'has been shipped',
  IN_TRANSIT: 'is now in transit. Track it live',
  ARRIVED: 'has arrived at the destination port',
  DELIVERED: 'has been delivered',
  REJECTED: 'was rejected at the port',
  CANCELLED: 'was cancelled',
};

@Injectable({ providedIn: 'root' })
export class StatusWatcher {
  private readonly auth = inject(Auth);
  private readonly api = inject(BookingApi);
  private readonly staffApi = inject(StaffApi);
  private readonly snack = inject(MatSnackBar);
  private timer?: ReturnType<typeof setInterval>;
  private running = false;

  start(): void {
    if (this.running) return;
    this.running = true;
    this.check();
    this.timer = setInterval(() => this.check(), 15000);
  }

  stop(): void {
    clearInterval(this.timer);
    this.running = false;
  }

  private isStaff(): boolean {
    return this.auth.isAdmin() || this.auth.user()?.role === 'EMPLOYEE';
  }

  private uid(): string {
    const u = this.auth.user() as { userId?: number; email?: string } | null;
    return String(u?.userId ?? u?.email ?? 'me');
  }

  private check(): void {
    if (!this.auth.user()) return;
    if (this.isStaff()) {
      this.staffApi.bookings().subscribe({ next: (b) => this.staffToasts(b), error: () => {} });
    } else {
      this.api.list({ page: 0, size: 100, status: '' }).subscribe({
        next: (r) => this.customerToasts(r.content), error: () => {},
      });
    }
  }

  // ---------------------------------------------------------------- customer
  private customerToasts(list: Booking[]): void {
    const key = `bookingStatus:${this.uid()}`;
    const known = this.load<Record<string, BookingStatus>>(key);
    const msgs: string[] = [];

    for (const b of list) {
      const before = known?.[b.bookingId];
      if (known && before && before !== b.status && TEXT[b.status]) {
        msgs.push(`Booking #${b.bookingId} ${TEXT[b.status]}.`);
      }
      if (this.isOverdue(b)) {
        const shown = this.load<string[]>(`overdue:${this.uid()}`) ?? [];
        const tag = `${b.bookingId}:${b.scheduleAt.slice(0, 10)}`;
        if (!shown.includes(tag)) {
          msgs.push(`Booking #${b.bookingId}: the departure time has passed but it has not shipped yet. Port staff have been reminded.`);
          this.save(`overdue:${this.uid()}`, [...shown, tag]);
        }
      }
    }
    this.save(key, Object.fromEntries(list.map((b) => [b.bookingId, b.status])));
    this.show(msgs);
  }

  // ------------------------------------------------------------------- staff
  private staffToasts(list: Booking[]): void {
    const late = list.filter((b) => this.isOverdue(b));
    if (!late.length) return;
    const shownKey = `overdueStaff:${this.uid()}`;
    const shown = this.load<string[]>(shownKey) ?? [];
    const fresh = late.filter((b) => !shown.includes(`${b.bookingId}:${b.scheduleAt.slice(0, 10)}`));
    if (!fresh.length) return;
    this.save(shownKey, [...shown, ...fresh.map((b) => `${b.bookingId}:${b.scheduleAt.slice(0, 10)}`)]);
    const containers = [...new Set(fresh.map((b) => b.containerId))];
    this.show([`Shipping is overdue for container ${containers.map((c) => '#' + c).join(', ')}. The departure time has passed and it is not shipped yet.`], 12000);
  }

  // ----------------------------------------------------------------- helpers
  private isOverdue(b: Booking): boolean {
    return (b.status === 'CONFIRMED' || b.status === 'AT_PORT') && new Date(b.scheduleAt) <= new Date();
  }

  private show(msgs: string[], ms = 8000): void {
    if (!msgs.length) return;
    const text = msgs.length > 2 ? `${msgs.slice(0, 2).join(' ')} (+${msgs.length - 2} more)` : msgs.join(' ');
    this.snack.open(text, 'Close', { duration: ms });
  }

  private load<T>(k: string): T | null {
    try { const v = localStorage.getItem(k); return v ? (JSON.parse(v) as T) : null; } catch { return null; }
  }
  private save(k: string, v: unknown): void {
    try { localStorage.setItem(k, JSON.stringify(v)); } catch { /* storage full or blocked */ }
  }
}