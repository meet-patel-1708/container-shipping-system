import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { filter, switchMap } from 'rxjs';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog/confirm-dialog';
import { FleetApi } from '../fleet.api';
import { FleetLookup } from '../fleet-lookup';
import { Port } from '../fleet.model';
import { pagedList } from '../paged-list';

@Component({
  selector: 'app-ports-tab',
  imports: [MatTableModule, MatSortModule, MatPaginatorModule, MatButtonModule, MatIconModule, MatProgressBarModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="fleet-head">
      <small>{{ list.total() }} ports</small>
      <button mat-flat-button (click)="openForm()"><mat-icon>add</mat-icon> New port</button>
    </div>
    @if (list.loading()) { <mat-progress-bar mode="indeterminate" /> }

    @if (list.error(); as err) {
      <div class="fleet-state"><mat-icon>error_outline</mat-icon><p>{{ err }}</p>
        <button mat-stroked-button (click)="list.reload()">Retry</button></div>
    } @else if (list.isEmpty()) {
      <div class="fleet-state"><mat-icon>anchor</mat-icon><p>No ports yet.</p>
        <button mat-flat-button (click)="openForm()">Add the first port</button></div>
    } @else {
      <div class="fleet-scroll">
        <table mat-table [dataSource]="list.items()" matSort (matSortChange)="onSort($event)">
          <ng-container matColumnDef="portCode">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="portCode">Code</th>
            <td mat-cell *matCellDef="let p">{{ p.portCode }}</td>
          </ng-container>
          <ng-container matColumnDef="portName">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="portName">Name</th>
            <td mat-cell *matCellDef="let p">{{ p.portName }}</td>
          </ng-container>
          <ng-container matColumnDef="portAddress">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="portAddress">Address</th>
            <td mat-cell *matCellDef="let p">{{ p.portAddress }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let p">
              <button mat-icon-button (click)="openForm(p)" [attr.aria-label]="'Edit ' + p.portName"><mat-icon>edit</mat-icon></button>
              <button mat-icon-button (click)="confirmDelete(p)" [attr.aria-label]="'Delete ' + p.portName"><mat-icon>delete</mat-icon></button>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="cols"></tr>
          <tr mat-row *matRowDef="let row; columns: cols"></tr>
        </table>
      </div>
      <mat-paginator [length]="list.total()" [pageSize]="list.size()" [pageIndex]="list.page()"
                     [pageSizeOptions]="[5, 10, 25]" (page)="onPage($event)" showFirstLastButtons />
    }
  `,
})
export class PortsTab {
  private readonly api = inject(FleetApi);
  private readonly lookup = inject(FleetLookup);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  protected readonly list = pagedList<Port>((q) => this.api.listPorts(q), 'portName,asc');
  protected readonly cols = ['portCode', 'portName', 'portAddress', 'actions'];

  protected onPage(e: PageEvent): void { this.list.setPage(e.pageIndex, e.pageSize); }
  protected onSort(e: Sort): void {
    this.list.setSort(e.direction ? `${e.active},${e.direction}` : this.list.defaultSort);
  }

  protected async openForm(port?: Port): Promise<void> {
    const { PortFormDialog } = await import('../fleet-dialogs');
    this.dialog.open(PortFormDialog, { width: '520px', maxWidth: '95vw', data: port ?? null })
      .afterClosed().subscribe((saved) => {
        if (!saved) return;
        this.snack.open(port ? 'Port updated' : 'Port created', 'Close', { duration: 3000 });
        this.list.reload();
        this.lookup.reload();
      });
  }

  protected confirmDelete(p: Port): void {
    this.dialog.open(ConfirmDialog, {
      data: {
        title: 'Delete port?',
        message: `${p.portName} (${p.portCode}) will be permanently deleted. Ships based here must be removed first.`,
        confirmLabel: 'Delete',
      },
    }).afterClosed().pipe(filter(Boolean), switchMap(() => this.api.deletePort(p.portCode)))
      .subscribe({
        next: () => { this.snack.open('Port deleted', 'Close', { duration: 3000 }); this.list.reload(); this.lookup.reload(); },
        error: (e: HttpErrorResponse) =>
          this.snack.open(e.status === 409 ? 'Cannot delete: this port is still in use.' : 'Delete failed.', 'Close', { duration: 4000 }),
      });
  }
}