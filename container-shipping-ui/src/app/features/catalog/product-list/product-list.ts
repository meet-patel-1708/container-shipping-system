import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { debounceTime, distinctUntilChanged, filter, switchMap } from 'rxjs';
import { ConfirmDialog } from '../../../shared/ui/confirm-dialog/confirm-dialog';
import { CatalogApi } from '../catalog.api';
import { Product, SORT_OPTIONS } from '../catalog.model';
import { CatalogStore } from '../catalog.store';

@Component({
  selector: 'app-product-list',
  imports: [
    ReactiveFormsModule, CurrencyPipe, DecimalPipe, MatCardModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatPaginatorModule, MatProgressBarModule,
  ],
  providers: [CatalogStore],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductList {
  protected readonly store = inject(CatalogStore);
  private readonly api = inject(CatalogApi);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  protected readonly sortOptions = SORT_OPTIONS;
  protected readonly skeletons = [1, 2, 3, 4, 5, 6];
  protected readonly search = new FormControl('', { nonNullable: true });

  private readonly failedImages = signal<ReadonlySet<number>>(new Set());
  protected readonly showImage = (p: Product) =>
    !!p.productImageUrl && !this.failedImages().has(p.productId);

  constructor() {
    this.search.valueChanges
      .pipe(debounceTime(350), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((q) => this.store.setQuery(q));
  }

  protected onPage(e: PageEvent): void {
    this.store.setPage(e.pageIndex, e.pageSize);
  }

  protected markImageFailed(id: number): void {
    this.failedImages.update((s) => new Set(s).add(id));
  }

  protected clearSearch(): void {
    this.search.setValue('');
  }

  /** The dialog (and its form code) is only downloaded the first time it's opened. */
  protected async openForm(product?: Product): Promise<void> {
    const { ProductFormDialog } = await import('../product-form-dialog/product-form-dialog');
    this.dialog
      .open(ProductFormDialog, { width: '560px', maxWidth: '95vw', data: product ?? null })
      .afterClosed()
      .subscribe((saved) => {
        if (!saved) return;
        this.snack.open(product ? 'Product updated' : 'Product created', 'Close', { duration: 3000 });
        this.store.reload();
      });
  }

  protected confirmDelete(p: Product): void {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: 'Delete product?',
          message: `"${p.productName}" (${p.productCode}) will be permanently deleted.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.api.delete(p.productId)),
      )
      .subscribe(() => {
        this.snack.open('Product deleted', 'Close', { duration: 3000 });
        this.store.reload();
      });
  }
}