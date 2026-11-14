import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { Container, ContainerType } from '../booking/booking.models';
import { ContainerApi } from './container.api';

@Component({
  selector: 'app-container-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule,
            MatSelectModule, MatButtonModule, MatProgressSpinnerModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: `
    .content { display: flex; flex-direction: column; gap: 4px; padding-top: 8px !important; }
    .row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
    .error { color: var(--mat-sys-error); margin: 8px 0 0; font-size: 13px; }
  `,
  template: `
    <h2 mat-dialog-title>New container</h2>
    <form [formGroup]="form" (ngSubmit)="submit()">
      <mat-dialog-content class="content">
        <mat-form-field appearance="outline">
          <mat-label>Company</mat-label>
          <input matInput formControlName="containerCompanyName" maxlength="100" />
          <mat-error>Company name is required (2-100 characters)</mat-error>
        </mat-form-field>
        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>Type</mat-label>
            <mat-select formControlName="containerType">
              @for (t of types; track t.value) { <mat-option [value]="t.value">{{ t.label }}</mat-option> }
            </mat-select>
            <mat-error>Pick a type</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Size (ft)</mat-label>
            <mat-select formControlName="containerSize">
              @for (s of sizes; track s) { <mat-option [value]="s">{{ s }} ft</mat-option> }
            </mat-select>
          </mat-form-field>
        </div>
        <mat-form-field appearance="outline">
          <mat-label>How many?</mat-label>
          <input matInput type="number" min="1" max="20" formControlName="count" />
          <mat-hint>Creates this many identical containers (max 20)</mat-hint>
          <mat-error>Whole number from 1 to 20</mat-error>
        </mat-form-field>
        @if (error()) { <p class="error">{{ error() }}</p> }
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button type="submit" [disabled]="saving()">
          @if (saving()) { <mat-spinner diameter="20" /> } @else { Create }
        </button>
      </mat-dialog-actions>
    </form>
  `,
})
export class ContainerDialog {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(ContainerApi);
  private readonly ref = inject<MatDialogRef<ContainerDialog, Container[]>>(MatDialogRef);

  protected readonly types: { value: ContainerType; label: string }[] = [
    { value: 'REGULAR', label: 'Regular' },
    { value: 'AIR_CONDITIONER_CONTAINER', label: 'Refrigerated / AC' },
  ];
  protected readonly sizes = [20, 40, 45];
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.group({
    containerCompanyName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    containerType: this.fb.control<ContainerType | ''>('REGULAR', Validators.required),
    containerSize: [20, Validators.required],
    count: [1, [Validators.required, Validators.min(1), Validators.max(20), Validators.pattern(/^\d+$/)]],
  });

  protected submit(): void {
    if (this.saving()) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    this.saving.set(true);
    this.error.set(null);
    this.api.create({
      containerCompanyName: v.containerCompanyName.trim(),
      containerType: v.containerType as ContainerType,
      containerSize: v.containerSize,
    }, Number(v.count)).subscribe({
      next: (created) => this.ref.close(created),
      error: (e: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(e.status === 0 ? 'Cannot reach the server.'
          : e.error?.validationErrors?.join('; ') ?? 'Could not create the containers.');
      },
    });
  }
}