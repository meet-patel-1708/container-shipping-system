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
  styleUrl: './dashboard.scss',
  templateUrl:'./dashboard.html' ,
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
