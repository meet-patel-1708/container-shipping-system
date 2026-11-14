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
import { VEHICLE_TYPES, Vehicle } from '../fleet.model';
import { pagedList } from '../paged-list';

@Component({
  selector: 'app-vehicles-tab',
  imports: [
    ReactiveFormsModule, MatTableModule, MatSortModule, MatPaginatorModule, MatButtonModule,
    MatIconModule, MatProgressBarModule, MatFormFieldModule, MatInputModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="fleet-head">
      <mat-form-field appearance="outline" subscriptSizing="dynamic" class="fleet-search">
        <mat-label>Search by name</mat-label>
        <input matInput [formControl]="search" />
        <mat-icon matSuffix>search</mat-icon>
      </mat-form-field>
      <button mat-flat-button (click)="openForm()"><mat-icon>add</mat-icon> New vehicle</button>
    </div>
    <small>{{ list.total() }} vehicles</small>
    @if (list.loading()) { <mat-progress-bar mode="indeterminate" /> }

    @if (list.error(); as err) {
      <div class="fleet-state"><mat-icon>error_outline</mat-icon><p>{{ err }}</p>
        <button mat-stroked-button (click)="list.reload()">Retry</button></div>
    } @else if (list.isEmpty()) {
      <div class="fleet-state"><mat-icon>local_shipping</mat-icon>
        <p>{{ search.value ? 'No vehicles match your search.' : 'No vehicles yet.' }}</p>
        @if (!search.value) { <button mat-flat-button (click)="openForm()">Add the first vehicle</button> }</div>
    } @else {
      <div class="fleet-scroll">
        <table mat-table [dataSource]="list.items()" matSort (matSortChange)="onSort($event)">
          <ng-container matColumnDef="vehicleNumber">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="vehicleNumber">Number</th>
            <td mat-cell *matCellDef="let v">{{ v.vehicleNumber }}</td>
          </ng-container>
          <ng-container matColumnDef="vehicleName">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="vehicleName">Name</th>
            <td mat-cell *matCellDef="let v">{{ v.vehicleName }}</td>
          </ng-container>
          <ng-container matColumnDef="vehicleType">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="vehicleType">Type</th>
            <td mat-cell *matCellDef="let v"><span class="badge">{{ typeLabel(v.vehicleType) }}</span></td>
          </ng-container>
          <ng-container matColumnDef="vehicleCapacity">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="vehicleCapacity">Capacity</th>
            <td mat-cell *matCellDef="let v">{{ v.vehicleCapacity }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef></th>
            <td mat-cell *matCellDef="let v">
              <button mat-icon-button (click)="openForm(v)" [attr.aria-label]="'Edit ' + v.vehicleNumber"><mat-icon>edit</mat-icon></button>
              <button mat-icon-button (click)="confirmDelete(v)" [attr.aria-label]="'Delete ' + v.vehicleNumber"><mat-icon>delete</mat-icon></button>
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
export class VehiclesTab {
  private readonly api = inject(FleetApi);
  private readonly lookup = inject(FleetLookup);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  protected readonly list = pagedList<Vehicle>((q) => this.api.listVehicles(q), 'vehicleName,asc');
  protected readonly cols = ['vehicleNumber', 'vehicleName', 'vehicleType', 'vehicleCapacity', 'actions'];
  protected readonly search = new FormControl('', { nonNullable: true });
  protected readonly typeLabel = (t: string) => VEHICLE_TYPES.find((x) => x.value === t)?.label ?? t;

  constructor() {
    this.search.valueChanges
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((q) => this.list.setQuery(q));
  }

  protected onPage(e: PageEvent): void { this.list.setPage(e.pageIndex, e.pageSize); }
  protected onSort(e: Sort): void {
    this.list.setSort(e.direction ? `${e.active},${e.direction}` : this.list.defaultSort);
  }

  protected async openForm(vehicle?: Vehicle): Promise<void> {
    const { VehicleFormDialog } = await import('../fleet-dialogs');
    this.dialog.open(VehicleFormDialog, { width: '560px', maxWidth: '95vw', data: vehicle ?? null })
      .afterClosed().subscribe((saved) => {
        if (!saved) return;
        this.snack.open(vehicle ? 'Vehicle updated' : 'Vehicle created', 'Close', { duration: 3000 });
        this.list.reload();
        this.lookup.reload();
      });
  }

  protected confirmDelete(v: Vehicle): void {
    this.dialog.open(ConfirmDialog, {
      data: { title: 'Delete vehicle?', message: `${v.vehicleName} (${v.vehicleNumber}) will be permanently deleted.`, confirmLabel: 'Delete' },
    }).afterClosed().pipe(filter(Boolean), switchMap(() => this.api.deleteVehicle(v.vehicleNumber)))
      .subscribe({
        next: () => { this.snack.open('Vehicle deleted', 'Close', { duration: 3000 }); this.list.reload(); this.lookup.reload(); },
        error: (e: HttpErrorResponse) =>
          this.snack.open(e.status === 409 ? 'Cannot delete: this vehicle is still in use.' : 'Delete failed.', 'Close', { duration: 4000 }),
      });
  }
}