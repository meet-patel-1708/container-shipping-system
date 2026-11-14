import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatStepperModule } from '@angular/material/stepper';
import { Router } from '@angular/router';
import { startWith } from 'rxjs';
import { Auth } from '../../../core/auth/auth';
import {
  DOC_LABELS, DriverApplication, DriverDocType, DriverType, REQUIRED_DOCS,
} from '../../../core/driver/driver.model';
import { DriverService } from '../../../core/driver/driver.service';

const ALLOWED = ['image/jpeg', 'image/png', 'application/pdf'];
const MAX_BYTES = 5 * 1024 * 1024;

@Component({
  selector: 'app-driver-apply',
  imports: [
    ReactiveFormsModule, MatStepperModule, MatRadioModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
  ],
  templateUrl: './driver-apply.html',
  styleUrl:'./driver-apply.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DriverApply {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(DriverService);
  private readonly auth = inject(Auth);
  private readonly router = inject(Router);

  protected readonly labels = DOC_LABELS;

  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly uploading = signal<DriverDocType | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly app = signal<DriverApplication | null>(null);
  protected readonly step = signal(0);

  protected readonly form = this.fb.group({
    driverType: this.fb.control<DriverType>('VEHICLE_DRIVER'),
    licenseNumber: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9 -]{5,30}$/)]],
    experienceYears: [0, [Validators.required, Validators.min(0), Validators.max(60)]],
    vehicleNumber: ['', [Validators.pattern(/^[A-Za-z0-9 -]{4,20}$/)]],
  });

  protected readonly isVehicle = toSignal(
    this.form.controls.driverType.valueChanges.pipe(startWith(this.form.controls.driverType.value)),
    { initialValue: 'VEHICLE_DRIVER' as DriverType },
  );

  protected readonly docRows = computed(() => {
    const a = this.app();
    return REQUIRED_DOCS[a?.driverType ?? 'VEHICLE_DRIVER'].map((type) => ({
      type,
      label: DOC_LABELS[type],
      doc: a?.documents.find((d) => d.documentType === type) ?? null,
    }));
  });

  protected readonly canSubmit = computed(() => {
    const a = this.app();
    return !!a && a.missingDocuments.length === 0;
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.api.mine().subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res) this.apply(res);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.msg(err));
      },
    });
  }

  private apply(res: DriverApplication): void {
    this.app.set(res);
    this.form.patchValue({
      driverType: res.driverType,
      licenseNumber: res.licenseNumber ?? '',
      experienceYears: res.experienceYears ?? 0,
      vehicleNumber: res.vehicleNumber ?? '',
    });
    // resume where the user left off
    this.step.set(res.missingDocuments.length > 0 ? 2 : 3);

    // approved, but the token still says USER: refresh so the role becomes DRIVER
    if (res.status === 'APPROVED' && this.auth.role() === 'USER') {
      this.auth.refresh().subscribe({ error: () => {} });
    }
  }

  protected saveDetails(): void {
    this.error.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const vehicle = v.driverType === 'VEHICLE_DRIVER';
    if (vehicle && !v.vehicleNumber.trim()) {
      this.form.controls.vehicleNumber.setErrors({ required: true });
      this.form.controls.vehicleNumber.markAsTouched();
      return;
    }

    this.saving.set(true);
    this.api.saveDraft({
      driverType: v.driverType,
      licenseNumber: v.licenseNumber.trim(),
      experienceYears: Number(v.experienceYears),
      vehicleNumber: vehicle ? v.vehicleNumber.trim() : '',
    }).subscribe({
      next: (res) => {
        this.saving.set(false);
        this.app.set(res);
        this.step.set(2);
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(this.msg(err));
      },
    });
  }

  protected onFile(type: DriverDocType, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';               // lets the user pick the same file again
    if (!file) return;

    this.error.set(null);
    if (!ALLOWED.includes(file.type)) {
      this.error.set('Only JPG, PNG or PDF files are allowed.');
      return;
    }
    if (file.size > MAX_BYTES) {
      this.error.set('File must be 5 MB or smaller.');
      return;
    }

    this.uploading.set(type);
    this.api.upload(type, file).subscribe({
      next: (res) => {
        this.uploading.set(null);
        this.app.set(res);
      },
      error: (err) => {
        this.uploading.set(null);
        this.error.set(this.msg(err));
      },
    });
  }

  protected submit(): void {
    this.error.set(null);
    this.saving.set(true);
    this.api.submit().subscribe({
      next: (res) => {
        this.saving.set(false);
        this.app.set(res);
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(this.msg(err));
      },
    });
  }

  protected checkStatus(): void {
    this.load();
  }

  protected goToDashboard(): void {
    const go = () => this.router.navigateByUrl('/dashboard');
    this.auth.refresh().subscribe({ next: go, error: go });
  }

  private msg(err: unknown): string {
    const e = err as HttpErrorResponse;
    if (e.status === 0) return 'Cannot reach the server.';
    return e.error?.message ?? 'Something went wrong. Please try again.';
  }
}