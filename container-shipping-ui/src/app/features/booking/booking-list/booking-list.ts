import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { filter, interval, switchMap } from 'rxjs';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog/confirm-dialog';
import { FleetLookup } from '../../fleet/fleet-lookup';
import { BookingApi } from '../booking.api';
import { Booking, STATUS_FILTERS } from '../booking.models';
import { BookingStore } from '../booking.store';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Auth } from '../../../core/auth/auth';

@Component({
  selector: 'app-booking-list',
  imports: [
    RouterLink, CurrencyPipe, MatButtonModule, MatButtonToggleModule, MatIconModule,
    MatPaginatorModule, MatProgressBarModule, MatTableModule,DatePipe
],
  providers: [BookingStore],
  templateUrl: './booking-list.html',
  styleUrl: './booking-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookingList {
  protected readonly store = inject(BookingStore);
  protected readonly fleet = inject(FleetLookup);
  private readonly api = inject(BookingApi);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
protected readonly auth = inject(Auth);
  protected readonly filters = STATUS_FILTERS;
  protected readonly cols = this.auth.isAdmin()
  ? ['id','route','ship','schedule','space','status','total','actions']
  : ['id','route','ship','schedule','status','total','actions'];

  constructor() {
    this.fleet.load();
    interval(10000).pipe(takeUntilDestroyed()).subscribe(() => this.store.reload());
  }

  protected canCancel(b: Booking): boolean {
  return b.status === 'PENDING' && !this.auth.isAdmin();
}

  protected onPage(e: PageEvent): void {
    this.store.setPage(e.pageIndex, e.pageSize);
  }

  protected cancel(b: Booking): void {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: 'Cancel booking?',
          message: `Booking #${b.bookingId} will be cancelled and the container released.`,
          confirmLabel: 'Cancel booking',
        },
      })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.api.cancel(b.bookingId)),
      )
      .subscribe(() => {
        this.snack.open('Booking cancelled', 'Close', { duration: 3000 });
        this.store.reload();
      });
  }
  
}