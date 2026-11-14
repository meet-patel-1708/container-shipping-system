import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { debounceTime, distinctUntilChanged, filter, switchMap } from 'rxjs';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog/confirm-dialog';
import { FleetApi } from '../fleet.api';
import { FleetLookup } from '../fleet-lookup';
import { Ship } from '../fleet.model';
import { pagedList } from '../paged-list';

@Component({
  selector: 'app-ships-tab',
  imports: [
    ReactiveFormsModule, MatTableModule, MatSortModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatProgressBarModule, MatFormFieldModule, MatInputModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="fleet-head">
      <mat-form-field appearance="outline" subscriptSizing="dynamic" class="fleet-search">
        <mat-label>Search by company</mat-label>
        <input matInput [formControl]="search" />
        <mat-icon matSuffix>search</mat-icon>
      </mat-form-field>
      <button mat-flat-button (click)="openForm()"><mat-icon>add</mat-icon> New ship</button>
    </div>
    <small>{{ list.total() }} ships</small>
    @if (list.loading()) { <mat-progress-bar mode="indeterminate" /> }

    @if (list.error(); as err) {
      <div class="fleet-state"><mat-icon>error_outline</mat-icon><p>{{ err }}</p>
        <button mat-stroked-button (click)="list.reload()">Retry</button></div>
    } @else if (list.isEmpty()) {
      <div class="fleet-state"><mat-icon>directions_boat</mat-icon>
        <p>{{ search.value ? 'No ships match your search.' : 'No ships yet.' }}</p>
        @if (!search.value) { <button mat-flat-button (click)="openForm()">Add the first ship</button> }</div>
    } @else {
      <div class="fleet-scroll">
        <table mat-table [dataSource]="list.items()" matSort (matSortChange)="onSort($event)">
          <ng-container matColumnDef="shipCode">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="shipCode">Code</th>
            <td mat-cell *matCellDef="let s">{{ s.shipCode }}</td>
          </ng-container>
          <ng-container matColumnDef="shipCompanyName">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="shipCompanyName">Company</th>
            <td mat-cell *matCellDef="let s">{{ s.shipCompanyName }}</td>
          </ng-container>
          <ng-container matColumnDef="shipCapacity">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="shipCapacity">Capacity</th>
            <td mat-cell *matCellDef="let s">{{ s.shipCapacity }}</td>
          </ng-container>
          <ng-container matColumnDef="port">
            <th mat-header-cell *matHeaderCellDef>Home port</th>
            <td mat-cell *matCellDef="let s">{{ s.port.portName }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let s">
              <button mat-icon-button (click)="openForm(s)" [attr.aria-label]="'Edit ' + s.shipCode"><mat-icon>edit</mat-icon></button>
              <button mat-icon-button (click)="confirmDelete(s)" [attr.aria-label]="'Delete ' + s.shipCode"><mat-icon>delete</mat-icon></button>
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
export class ShipsTab {
  private readonly api = inject(FleetApi);
  private readonly lookup = inject(FleetLookup);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  protected readonly list = pagedList<Ship>((q) => this.api.listShips(q), 'shipCompanyName,asc');
  protected readonly cols = ['shipCode', 'shipCompanyName', 'shipCapacity', 'port', 'actions'];
  protected readonly search = new FormControl('', { nonNullable: true });

  constructor() {
    this.search.valueChanges
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((q) => this.list.setQuery(q));
  }

  protected onPage(e: PageEvent): void { this.list.setPage(e.pageIndex, e.pageSize); }
  protected onSort(e: Sort): void {
    this.list.setSort(e.direction ? `${e.active},${e.direction}` : this.list.defaultSort);
  }

  protected async openForm(ship?: Ship): Promise<void> {
    const { ShipFormDialog } = await import('../fleet-dialogs');
    this.dialog.open(ShipFormDialog, { width: '560px', maxWidth: '95vw', data: ship ?? null })
      .afterClosed().subscribe((saved) => {
        if (!saved) return;
        this.snack.open(ship ? 'Ship updated' : 'Ship created', 'Close', { duration: 3000 });
        this.list.reload();
        this.lookup.reload();
      });
  }

  protected confirmDelete(s: Ship): void {
    this.dialog.open(ConfirmDialog, {
      data: { title: 'Delete ship?', message: `${s.shipCompanyName} (${s.shipCode}) will be permanently deleted.`, confirmLabel: 'Delete' },
    }).afterClosed().pipe(filter(Boolean), switchMap(() => this.api.deleteShip(s.shipCode)))
      .subscribe({
        next: () => { this.snack.open('Ship deleted', 'Close', { duration: 3000 }); this.list.reload(); this.lookup.reload(); },
        error: (e: HttpErrorResponse) =>
          this.snack.open(e.status === 409 ? 'Cannot delete: this ship is still in use.' : 'Delete failed.', 'Close', { duration: 4000 }),
      });
  }
}