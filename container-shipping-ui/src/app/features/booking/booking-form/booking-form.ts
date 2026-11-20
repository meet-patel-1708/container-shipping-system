import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormArray, NonNullableFormBuilder, ReactiveFormsModule, ValidatorFn, Validators,
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
import { catchError, map, of } from 'rxjs';
import { CatalogApi } from '../../catalog/catalog.api';
import { Product } from '../../catalog/catalog.model';
import { FleetLookup } from '../../fleet/fleet-lookup';
import { BookingApi } from '../booking.api';
import { BookingRequest, Container } from '../booking.models';
import { Auth } from '../../../core/auth/auth';
import { ProfileService } from '../../../core/profile/profile.service';
import {MatCheckboxModule} from '@angular/material/checkbox';
const differentPorts: ValidatorFn = (g) => {
  const o = g.get('originPortId')?.value;
  const d = g.get('destPortId')?.value;
  return o != null && d != null && o === d ? { samePort: true } : null;
};
const uniqueProducts: ValidatorFn = (arr) => {
  const ids = (arr as FormArray).controls.map((c) => c.get('productId')?.value).filter((v) => v != null);
  return new Set(ids).size !== ids.length ? { duplicateProduct: true } : null;
};
const localNow = () => new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 16);
const notPast: ValidatorFn = (c) => (c.value && new Date(c.value) <= new Date() ? { past: true } : null);
// in the class:

// form: schedule: this.fb.group({ scheduleAt: this.fb.control('', [Validators.required, notPast]) }),
// submit body: scheduleAt: v.schedule.scheduleAt.length === 16 ? v.schedule.scheduleAt + ':00' : v.schedule.scheduleAt,

@Component({
  selector: 'app-booking-form',
  imports: [
    ReactiveFormsModule, CurrencyPipe, DecimalPipe, MatStepperModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule, RouterLink,DatePipe
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
    inject(ProfileService).eligibility().pipe(catchError(() => of(null))),
    { initialValue: null },
  );

  // Only the user's own products come back (catalog filters by owner). First 100 only.
  protected readonly products = toSignal(
    inject(CatalogApi)
      .list({ page: 0, size: 100, sort: 'productName,asc', query: '' })
      .pipe(map((r) => r.content), catchError(() => of([] as Product[]))),
    { initialValue: [] as Product[] },
  );
  protected readonly containers = toSignal(
    this.bookingApi.availableContainers().pipe(catchError(() => of([] as Container[]))),
    { initialValue: [] as Container[] },
  );

  private newItem() {
    return this.fb.group({
      productId: this.fb.control<number | null>(null, Validators.required),
      quantity: this.fb.control(1, [
        Validators.required, Validators.min(1), Validators.max(10_000), Validators.pattern(/^\d+$/),
      ]),
    });
  }

  protected readonly form = this.fb.group({
    route: this.fb.group(
      {
        originPortId: this.fb.control<number | null>(null, Validators.required),
        destPortId: this.fb.control<number | null>(null, Validators.required),
        shipId: this.fb.control<number | null>(null, Validators.required),
        containerId: this.fb.control<number | null>(null, Validators.required),
        scheduleAt: this.fb.control('', [Validators.required, notPast])
      },
      { validators: differentPorts },
    ),
    items: this.fb.array([this.newItem()], [Validators.required, uniqueProducts]),
    terms: this.fb.control(false, Validators.requiredTrue),
    schedule: this.fb.group({ scheduleDate: this.fb.control('', [Validators.required, notPast]) }),
  });

  protected readonly items = this.form.controls.items;

  // Form values aren't signals in Angular 20, so mirror them for OnPush/zoneless templates
  private readonly value = toSignal(this.form.valueChanges.pipe(map(() => this.form.getRawValue())), {
    initialValue: this.form.getRawValue(),
  });

  protected readonly lines = computed(() =>
    this.value().items.map((it) => {
      const p = this.products().find((x) => x.productId === it.productId);
      const qty = Number(it.quantity) || 0;
      const price = p?.productPrice ?? 0;
      return {
        name: p?.productName ?? '—', code: p?.productCode ?? '', qty, price,
        subtotal: price * qty, weight: (p?.productWeight ?? 0) * qty,
      };
    }),
  );
  protected readonly totalWeight = computed(() => this.lines().reduce((s, l) => s + l.weight, 0));
  protected readonly selectedContainer = computed(() =>
    this.containers().find((c) => c.containerId === this.value().route.containerId) ?? null);
  protected readonly overweight = computed(() => {
    const c = this.selectedContainer();
    return !!c && this.totalWeight() > c.remainingKg;
  });
  protected readonly blocked = computed(() => this.eligibility()?.eligible === false || this.overweight());
  protected readonly total = computed(() => this.lines().reduce((s, l) => s + l.subtotal, 0));
  protected readonly review = computed(() => ({ ...this.value().route, ...this.value().schedule }));

  constructor() {
    this.fleet.load();
  }

  protected containerLabel(c: Container): string {
    const type = c.containerType === 'REGULAR' ? 'Regular' : 'Refrigerated/AC';
    return `#${c.containerId} · ${type} · ${c.containerSize}ft · ${c.remainingKg} of ${c.capacityKg} kg free`;
  }

  protected taken(productId: number, index: number): boolean {
    return this.value().items.some((it, i) => i !== index && it.productId === productId);
  }
protected readonly minDateTime = localNow();

  protected addItem(): void { this.items.push(this.newItem()); }

  protected removeItem(i: number): void {
    if (this.items.length > 1) this.items.removeAt(i);
  }

  protected submit(): void {
    if (this.saving()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }


    const v = this.form.getRawValue();
    const body: BookingRequest = {
      containerId: v.route.containerId!, shipId: v.route.shipId!,
      originPortId: v.route.originPortId!, destPortId: v.route.destPortId!,
      items: v.items.map((i) => ({ productId: i.productId!, quantity: Number(i.quantity) })),
      scheduleAt: v.schedule.scheduleDate.length === 16 ? v.schedule.scheduleDate + ':00' : v.schedule.scheduleDate,
    };

    this.saving.set(true);
    this.serverErrors.set([]);
    this.bookingApi.create(body).subscribe({
      next: (saved) => {
        this.snack.open('Booking created', 'Close', { duration: 3000 });
        this.router.navigate(['/bookings', saved.bookingId]);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        const e = err.error;
        this.serverErrors.set(
          err.status === 0 ? ['Cannot reach the server.']
          : e?.validationErrors?.length ? e.validationErrors
          : [e?.message ?? 'Could not create the booking.'],
        );
      },
    });
  }
}

