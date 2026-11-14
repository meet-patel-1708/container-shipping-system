import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CatalogApi } from '../catalog.api';
import { ApiError, Product, ProductRequest } from '../catalog.model';
import { productCodeAvailable } from '../product-code.validator';

@Component({
  selector: 'app-product-form-dialog',
  imports: [
    ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule,
  ],
  templateUrl: './product-form-dialog.html',
  styleUrl: './product-form-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductFormDialog {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(CatalogApi);
  private readonly ref = inject<MatDialogRef<ProductFormDialog, Product>>(MatDialogRef);
  protected readonly product = inject<Product | null>(MAT_DIALOG_DATA);

  protected readonly isEdit = this.product !== null;
  protected readonly saving = signal(false);
  protected readonly serverErrors = signal<string[]>([]);

  protected readonly form = this.fb.group({
    // Code is immutable once created: changing it would hit the DB unique constraint and bust caches
    productCode: this.fb.control(
      { value: this.product?.productCode ?? '', disabled: this.isEdit },
      {
        validators: [Validators.required, Validators.pattern(/^PC\d{5}$/)],
        asyncValidators: this.isEdit ? [] : [productCodeAvailable(this.api)],
      },
    ),
    productName: [this.product?.productName ?? '', [Validators.required, Validators.maxLength(100)]],
    productDescription: [this.product?.productDescription ?? '', [Validators.required, Validators.maxLength(500)]],
    productPrice: this.fb.control<number | null>(this.product?.productPrice ?? null, [
      Validators.required, Validators.min(0), Validators.max(99_999_999.99),
      Validators.pattern(/^\d+(\.\d{1,2})?$/),
    ]),
    productWeight: this.fb.control<number | null>(this.product?.productWeight ?? null, [
      Validators.required, Validators.min(0), Validators.max(999_999.99),
      Validators.pattern(/^\d+(\.\d{1,2})?$/),
    ]),
    productImageUrl: [this.product?.productImageUrl ?? '', [Validators.maxLength(500), Validators.pattern(/^$|^https?:\/\/\S+$/)]],
  });

  protected readonly c = this.form.controls;

  // Forms status isn't a signal in Angular 20, so mirror it for OnPush/zoneless templates
  protected readonly status = toSignal(this.form.statusChanges, { initialValue: this.form.status });
  protected readonly imageUrl = toSignal(this.c.productImageUrl.valueChanges, {
    initialValue: this.c.productImageUrl.value,
  });
  protected readonly imageOk = signal(true);

  protected upperCode(): void {
    const ctrl = this.c.productCode;
    const upper = ctrl.value.toUpperCase();
    if (upper !== ctrl.value) ctrl.setValue(upper);
  }

  protected submit(): void {
    if (this.saving() || this.form.pending) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue(); // includes the disabled code control in edit mode
    const body: ProductRequest = {
      productCode: v.productCode.trim(),
      productName: v.productName.trim(),
      productDescription: v.productDescription.trim(),
      productPrice: v.productPrice!,
      productWeight: v.productWeight!,
      productImageUrl: v.productImageUrl.trim() || null,
    };

    this.saving.set(true);
    this.serverErrors.set([]);

    const request$ = this.product
      ? this.api.update(this.product.productId, body)
      : this.api.create(body);

    request$.subscribe({
      next: (saved) => this.ref.close(saved),
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        const e = err.error as ApiError | null;
        this.serverErrors.set(
          err.status === 0 ? ['Cannot reach the server.']
          : e?.validationErrors?.length ? e.validationErrors
          : [e?.message ?? 'Could not save the product.'],
        );
      },
    });
  }
}