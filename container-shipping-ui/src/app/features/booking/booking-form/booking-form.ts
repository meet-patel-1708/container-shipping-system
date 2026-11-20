
import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormArray,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { MatCheckboxModule } from '@angular/material/checkbox';

import { catchError, map, of } from 'rxjs';

import { CatalogApi } from '../../catalog/catalog.api';
import { Product } from '../../catalog/catalog.model';
import { FleetLookup } from '../../fleet/fleet-lookup';
import { BookingApi } from '../booking.api';
import { BookingRequest, Container } from '../booking.models';
import { Auth } from '../../../core/auth/auth';
import { ProfileService } from '../../../core/profile/profile.service';

// Ensure origin and destination ports are different
const differentPorts: ValidatorFn = (g) => {
  const o = g.get('originPortId')?.value;
  const d = g.get('destPortId')?.value;

  return o != null && d != null && o === d
    ? { samePort: true }
    : null;
};

// Prevent duplicate products in booking items
const uniqueProducts: ValidatorFn = (arr) => {
  const ids = (arr as FormArray).controls
    .map((c) => c.get('productId')?.value)
    .filter((v) => v != null);

  return new Set(ids).size !== ids.length
    ? { duplicateProduct: true }
    : null;
};

// Local date/time for datetime-local input
const localNow = () =>
  new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16);

// Prevent scheduling in the past
const notPast: ValidatorFn = (c) =>
  c.value && new Date(c.value) <= new Date()
    ? { past: true }
    : null;

@Component({
  selector: 'app-booking-form',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    DecimalPipe,
    DatePipe,
    MatStepperModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCheckboxModule,
    RouterLink,
  ],
  templateUrl: './booking-form.html',
  styleUrl: './booking-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookingForm {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly bookingApi = inject(BookingApi);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);

  protected readonly fleet = inject(FleetLookup);
  protected readonly auth = inject(Auth);

  protected readonly saving = signal(false);
  protected readonly serverErrors = signal<string[]>([]);

  protected readonly eligibility = toSignal(
    inject(ProfileService)
      .eligibility()
      .pipe(catchError(() => of(null))),
    { initialValue: null },
  );

  // Only the user's own products come back (catalog filters by owner).
  // First 100 products.
  protected readonly products = toSignal(
    inject(CatalogApi)
      .list({
        page: 0,
        size: 100,
        sort: 'productName,asc',
        query: '',
      })
      .pipe(
        map((r) => r.content),
        catchError(() => of([] as Product[])),
      ),
    { initialValue: [] as Product[] },
  );

  // Available containers
  protected readonly containers = toSignal(
    this.bookingApi
      .availableContainers()
      .pipe(catchError(() => of([] as Container[]))),
    { initialValue: [] as Container[] },
  );

  // Create a new booking item form group
  private newItem() {
    return this.fb.group({
      productId: this.fb.control<number | null>(
        null,
        Validators.required,
      ),

      quantity: this.fb.control(1, [
        Validators.required,
        Validators.min(1),
        Validators.max(10_000),
        Validators.pattern(/^\d+$/),
      ]),
    });
  }

  // Main booking form
  protected readonly form = this.fb.group({
    route: this.fb.group(
      {
        originPortId: this.fb.control<number | null>(
          null,
          Validators.required,
        ),

        destPortId: this.fb.control<number | null>(
          null,
          Validators.required,
        ),

        shipId: this.fb.control<number | null>(
          null,
          Validators.required,
        ),

        containerId: this.fb.control<number | null>(
          null,
          Validators.required,
        ),
      },
      { validators: differentPorts },
    ),

    items: this.fb.array(
      [this.newItem()],
      [Validators.required, uniqueProducts],
    ),

    terms: this.fb.control(false, Validators.requiredTrue),

    schedule: this.fb.group({
      scheduleDate: this.fb.control('', [
        Validators.required,
        notPast,
      ]),
    }),
  });

  protected readonly items = this.form.controls.items;

  // Mirror form values for OnPush/zoneless templates
  private readonly value = toSignal(
    this.form.valueChanges.pipe(
      map(() => this.form.getRawValue()),
    ),
    {
      initialValue: this.form.getRawValue(),
    },
  );

  // Booking line details
  protected readonly lines = computed(() =>
    this.value().items.map((it) => {
      const p = this.products().find(
        (x) => x.productId === it.productId,
      );

      const qty = Number(it.quantity) || 0;
      const price = p?.productPrice ?? 0;

      return {
        name: p?.productName ?? '—',
        code: p?.productCode ?? '',
        qty,
        price,
        subtotal: price * qty,
        weight: (p?.productWeight ?? 0) * qty,
      };
    }),
  );

  // Total cargo weight
  protected readonly totalWeight = computed(() =>
    this.lines().reduce((s, l) => s + l.weight, 0),
  );

  // Selected container
  protected readonly selectedContainer = computed(
    () =>
      this.containers().find(
        (c) =>
          c.containerId === this.value().route.containerId,
      ) ?? null,
  );

  // Check if cargo exceeds container capacity
  protected readonly overweight = computed(() => {
    const c = this.selectedContainer();

    return !!c && this.totalWeight() > c.remainingKg;
  });

  // Booking eligibility or weight restriction
  protected readonly blocked = computed(
    () =>
      this.eligibility()?.eligible === false ||
      this.overweight(),
  );

  // Total booking amount
  protected readonly total = computed(() =>
    this.lines().reduce((s, l) => s + l.subtotal, 0),
  );

  // Review information
  protected readonly review = computed(() => ({
    ...this.value().route,
    ...this.value().schedule,
  }));

  // Minimum date/time for scheduling
  protected readonly minDateTime = localNow();

  constructor() {
    this.fleet.load();
  }

  // Container display label
  protected containerLabel(c: Container): string {
    const type =
      c.containerType === 'REGULAR'
        ? 'Regular'
        : 'Refrigerated/AC';

    return `#${c.containerId} · ${type} · ${c.containerSize}ft · ${c.remainingKg} of ${c.capacityKg} kg free`;
  }

  // Prevent selecting the same product twice
  protected taken(productId: number, index: number): boolean {
    return this.value().items.some(
      (it, i) => i !== index && it.productId === productId,
    );
  }

  // Add booking item
  protected addItem(): void {
    this.items.push(this.newItem());
  }

  // Remove booking item
  protected removeItem(i: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(i);
    }
  }

  // Submit booking
  protected submit(): void {
    if (this.saving()) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // Get form values before creating request body
    const v = this.form.getRawValue();

    // Booking API request
    const body: BookingRequest = {
      containerId: v.route.containerId!,
      shipId: v.route.shipId!,
      originPortId: v.route.originPortId!,
      destPortId: v.route.destPortId!,

      items: v.items.map((i) => ({
        productId: i.productId!,
        quantity: Number(i.quantity),
      })),

      scheduleAt:
        v.schedule.scheduleDate.length === 16
          ? v.schedule.scheduleDate + ':00'
          : v.schedule.scheduleDate,

      acceptedTerms: v.terms,
    };

    this.saving.set(true);
    this.serverErrors.set([]);

    this.bookingApi.create(body).subscribe({
      next: (saved) => {
        this.snack.open('Booking created', 'Close', {
          duration: 3000,
        });

        this.router.navigate([
          '/bookings',
          saved.bookingId,
        ]);
      },

      error: (err: HttpErrorResponse) => {
        this.saving.set(false);

        const e = err.error;

        this.serverErrors.set(
          err.status === 0
            ? ['Cannot reach the server.']
            : e?.validationErrors?.length
              ? e.validationErrors
              : [
                  e?.message ??
                    'Could not create the booking.',
                ],
        );
      },
    });
  }
}
