import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import {
  Address, AddressInput, DocumentType, Profile as ProfileData, ProfileService,
} from '../../../core/profile/profile.service';

const DOC_FORMAT: Record<DocumentType, RegExp> = {
  AADHAR: /^[2-9][0-9]{11}$/,
  PAN: /^[A-Z]{5}[0-9]{4}[A-Z]$/,
  PASSPORT: /^[A-PR-WY][0-9]{7}$/,
  DRIVING_LICENSE: /^[A-Z]{2}[0-9A-Z]{11,16}$/,
  VOTER_ID: /^[A-Z]{3}[0-9]{7}$/,
};

const DOC_HINT: Record<DocumentType, string> = {
  AADHAR: '12 digits',
  PAN: 'Format ABCDE1234F',
  PASSPORT: 'Format A1234567',
  DRIVING_LICENSE: 'e.g. GJ0520230001234',
  VOTER_ID: 'Format ABC1234567',
};

const letters = Validators.pattern(/^[A-Za-z ]+$/);

function docFormat(group: AbstractControl): ValidationErrors | null {
  const type = group.get('documentType')?.value as DocumentType;
  const num = String(group.get('documentNumber')?.value ?? '').trim().toUpperCase();
  return num && !DOC_FORMAT[type].test(num) ? { format: true } : null;
}

@Component({
  selector: 'app-profile',
  imports: [
    DatePipe, ReactiveFormsModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatProgressSpinnerModule,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Profile {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(ProfileService);

  protected readonly docTypes = Object.keys(DOC_FORMAT) as DocumentType[];
  protected readonly hints = DOC_HINT;

  protected readonly data = signal<ProfileData | null>(null);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);

  protected readonly showAddress = signal(false);
  protected readonly editingId = signal<number | null>(null);

  protected readonly isCustomerLike = computed(() => {
    const r = this.data()?.user.role;
    return r === 'USER' || r === 'DRIVER'||r === 'EMPLOYEE';
  });

  protected readonly missing = computed(() => {
    const d = this.data();
    if (!d || d.user.role !== 'USER') return [];
    const out: string[] = [];
    if (d.addresses.length === 0) out.push('an address');
    if (!d.kyc || d.kyc.documents.length === 0) out.push('a KYC document');
    return out;
  });

  protected readonly addressForm = this.fb.group({
    street: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    city: ['', [Validators.required, Validators.maxLength(100), letters]],
    state: ['', [Validators.required, Validators.maxLength(100), letters]],
    country: ['India', [Validators.required, Validators.maxLength(50), letters]],
    pincode: ['', [Validators.required, Validators.pattern(/^[1-9][0-9]{5}$/)]],
  });

  protected readonly kycForm = this.fb.group(
    {
      documentType: this.fb.control<DocumentType>('AADHAR'),
      documentNumber: ['', Validators.required],
    },
    { validators: docFormat },
  );

  protected readonly kycType = signal<DocumentType>('AADHAR');

  constructor() {
    this.load();
    this.kycForm.controls.documentType.valueChanges.subscribe((t) => this.kycType.set(t));
  }

  protected load(): void {
    this.api.profile().subscribe({
      next: (p) => { this.data.set(p); this.loading.set(false); },
      error: (e) => { this.loading.set(false); this.error.set(this.msg(e)); },
    });
  }

  // ---------------------------------------------------------------- address

  protected newAddress(): void {
    this.editingId.set(null);
    this.addressForm.reset({ street: '', city: '', state: '', country: 'India', pincode: '' });
    this.showAddress.set(true);
  }

  protected editAddress(a: Address): void {
    this.editingId.set(a.addressId);
    this.addressForm.reset({
      street: a.street, city: a.city, state: a.state, country: a.country, pincode: a.pincode,
    });
    this.showAddress.set(true);
  }

  protected saveAddress(): void {
    this.clear();
    if (this.addressForm.invalid) { this.addressForm.markAllAsTouched(); return; }
    const body = this.addressForm.getRawValue() as AddressInput;
    const id = this.editingId();
    this.busy.set(true);
    (id === null ? this.api.addAddress(body) : this.api.updateAddress(id, body)).subscribe({
      next: () => {
        this.busy.set(false);
        this.showAddress.set(false);
        this.info.set('Address saved.');
        this.load();
      },
      error: (e) => this.fail(e),
    });
  }

  protected removeAddress(a: Address): void {
    this.clear();
    this.busy.set(true);
    this.api.deleteAddress(a.addressId).subscribe({
      next: () => { this.busy.set(false); this.load(); },
      error: (e) => this.fail(e),
    });
  }

  // -------------------------------------------------------------------- kyc

  protected saveKyc(): void {
    this.clear();
    if (this.kycForm.invalid) { this.kycForm.markAllAsTouched(); return; }
    const v = this.kycForm.getRawValue();
    this.busy.set(true);
    this.api.addKyc({ documentType: v.documentType, documentNumber: v.documentNumber.trim().toUpperCase() })
      .subscribe({
        next: () => {
          this.busy.set(false);
          this.kycForm.controls.documentNumber.reset('');
          this.info.set('Document saved. It will be reviewed by an admin.');
          this.load();
        },
        error: (e) => this.fail(e),
      });
  }

  protected removeKyc(id: number): void {
    this.clear();
    this.busy.set(true);
    this.api.deleteKyc(id).subscribe({
      next: () => { this.busy.set(false); this.load(); },
      error: (e) => this.fail(e),
    });
  }

  // ---------------------------------------------------------------- helpers

  private clear(): void { this.error.set(null); this.info.set(null); }

  private fail(e: unknown): void { this.busy.set(false); this.error.set(this.msg(e)); }

  private msg(err: unknown): string {
    const e = err as HttpErrorResponse;
    if (e.status === 0) return 'Cannot reach the server.';
    return e.error?.message ?? 'Something went wrong.';
  }
}