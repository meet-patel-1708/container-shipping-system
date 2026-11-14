import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { catchError, interval, of, startWith, switchMap } from 'rxjs';
import { Auth } from '../../../core/auth/auth';
import { BookingApi } from '../../booking/booking.api';
import { BookingSummary, ContainerLoad } from '../../booking/booking.models';
import { StaffApi } from '../../../core/staff/staff-api';

@Component({
  selector: 'app-dashboard',
  imports: [DatePipe, RouterLink, MatCardModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
    .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-top: 16px; }
    .num { font-size: 32px; font-weight: 600; margin: 8px 0 0; }
    .wrap { padding: 16px; }
  `],
  template: `
    <div class="wrap">
      <h2>Welcome, {{ user()?.name }} 👋</h2>
        <div class="stats">
          @if (isStaff()) {
  <h3>Container space</h3>
  @for (c of load(); track c.containerId) {
    <p>Container #{{ c.containerId }}: <strong>{{ c.remainingKg }} kg free</strong> of {{ c.capacityKg }} kg ({{ c.allocatedKg }} kg booked)</p>
  } @empty { <p>No container has bookings yet.</p> }
}
          <mat-card appearance="outlined"><mat-card-content>
            Active bookings<p class="num">{{ summary()?.activeBookings ?? '—' }}</p>
          </mat-card-content></mat-card>
          <mat-card appearance="outlined"><mat-card-content>
            Containers in transit<p class="num">{{ summary()?.containersInTransit ?? '—' }}</p>
          </mat-card-content></mat-card>
          <mat-card appearance="outlined"><mat-card-content>
            Pending payments<p class="num">{{ summary()?.pendingPayments ?? '—' }}</p>
          </mat-card-content></mat-card>
          <mat-card appearance="outlined"><mat-card-content>
            Member since<p class="num">{{ user()?.createdAt | date: 'MMM d, y' }}</p>
          </mat-card-content></mat-card>
        </div>
        @if (user()?.role === 'USER') {
          <p><a mat-flat-button routerLink="/bookings/new">New booking</a>
             <a mat-button routerLink="/tracking">Live tracking</a></p>
        }
      
    </div>
  `,
})
export class Dashboard {
  protected readonly user = inject(Auth).user;
  private readonly api = inject(BookingApi);
private readonly staffApi = inject(StaffApi);
protected readonly auth = inject(Auth);
protected readonly isStaff = computed(() => this.user()?.role === 'EMPLOYEE' || this.auth.isAdmin());
protected readonly load = toSignal(
  interval(15000).pipe(startWith(0), switchMap(() =>
    this.isStaff() ? this.staffApi.containerLoad().pipe(catchError(() => of([] as ContainerLoad[]))) : of([] as ContainerLoad[]))),
  { initialValue: [] as ContainerLoad[] });
  protected readonly summary = toSignal(
    interval(15000).pipe(
      startWith(0),
      switchMap(() => this.api.summary().pipe(catchError(() => of(null as BookingSummary | null)))),
    ),
    { initialValue: null as BookingSummary | null },
  );
}