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
import { PAYMENT_FILTERS, Payment } from '../payment.models';
import { PaymentStore } from '../payment.store';
import { Auth } from '../../../core/auth/auth';

@Component({
  selector: 'app-payment-list',
  imports: [
    RouterLink, DatePipe, CurrencyPipe, MatButtonModule, MatButtonToggleModule, MatIconModule,
    MatPaginatorModule, MatProgressBarModule, MatTableModule,
  ],
  providers: [PaymentStore],
  templateUrl: './payment-list.html',
  styleUrl: './payment-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentList {
  protected readonly store = inject(PaymentStore);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  protected readonly filters = PAYMENT_FILTERS;
  protected readonly admin = inject(Auth).isAdmin;
protected readonly cols = this.admin()
  ? ['id', 'user', 'booking', 'amount', 'method', 'status', 'paidAt']
  : ['id', 'booking', 'amount', 'method', 'status', 'paidAt', 'actions'];

  protected onPage(e: PageEvent): void {
    this.store.setPage(e.pageIndex, e.pageSize);
  }

  /** Dialog code is downloaded only the first time someone pays. */
  protected async pay(p: Payment): Promise<void> {
    const { PayDialog } = await import('../pay-dialog/pay-dialog');
    this.dialog
      .open(PayDialog, { width: '420px', maxWidth: '95vw', data: p })
      .afterClosed()
      .subscribe((paid) => {
        if (!paid) return;
        this.snack.open('Payment successful. Your booking will be confirmed shortly.', 'Close', { duration: 4000 });
        this.store.reload();
      });
  }
}