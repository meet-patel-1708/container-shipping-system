import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AsyncValidatorFn, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { map, Observable, of, switchMap, timer } from 'rxjs';
import { FleetApi } from './fleet.api';
import { FleetLookup } from './fleet-lookup';
import {
  Port, PortRequest, Ship, ShipRequest, VEHICLE_TYPES, Vehicle, VehicleRequest,
} from './fleet.model';

const PORT_CODE = /^PC\d{5}$/;
const SHIP_CODE = /^SH\d{5}$/;
const VEHICLE_NO = /^[A-Z0-9-]{4,20}$/;

/** Debounced: Angular cancels the previous run when the value changes, so the timer cancels it. */
function uniqueValue(check: (v: string) => Observable<boolean>, pattern: RegExp): AsyncValidatorFn {
  return (c) => {
    const v = (c.value ?? '') as string;
    if (!pattern.test(v)) return of(null);
    return timer(400).pipe(switchMap(() => check(v)), map((taken) => (taken ? { taken: true } : null)));
  };
}

function describeError(e: HttpErrorResponse, conflict: string, notFound: string): string[] {
  if (e.status === 0) return ['Cannot reach the server.'];
  if (e.status === 401 || e.status === 403) return ['You are not allowed to do that.'];
  const list = e.error?.validationErrors as string[] | undefined;
  if (list?.length) return list;
  if (e.status === 409) return [conflict];
  if (e.status === 404) return [notFound];
  return ['Could not save. Please try again.'];
}

abstract class DialogBase<T> {
  protected readonly ref = inject<MatDialogRef<unknown, T>>(MatDialogRef);
  protected readonly saving = signal(false);
  protected readonly errors = signal<string[]>([]);
  protected abstract readonly conflictMsg: string;
  protected abstract readonly notFoundMsg: string;

  protected run(request$: Observable<T>): void {
    this.saving.set(true);
    this.errors.set([]);
    request$.subscribe({
      next: (v) => this.ref.close(v),
      error: (e: HttpErrorResponse) => {
        this.saving.set(false);
        this.errors.set(describeError(e, this.conflictMsg, this.notFoundMsg));
      },
    });
  }
}

const DIALOG_STYLES = `
  .content { display: flex; flex-direction: column; gap: 4px; padding-top: 8px !important; }
  .row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
  .error { color: var(--mat-sys-error); margin: 8px 0 0; padding-left: 18px; font-size: 13px; }
`;
const COMMON = [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule];

// ================= PORT =================
@Component({
  selector: 'app-port-form-dialog',
  imports: COMMON,
  styles: DIALOG_STYLES,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit port' : 'New port' }}</h2>
    <form [formGroup]="form" (ngSubmit)="submit()">
      <mat-dialog-content class="content">
        <mat-form-field appearance="outline">
          <mat-label>Port code</mat-label>
          <input matInput formControlName="portCode" maxlength="7" (input)="upper()" />
          <mat-hint>{{ status() === 'PENDING' ? 'Checking availability…' : 'Format: PC12345' }}</mat-hint>
          @if (c.portCode.hasError('required')) { <mat-error>Code is required</mat-error> }
          @else if (c.portCode.hasError('pattern')) { <mat-error>Use PC followed by 5 digits</mat-error> }
          @else if (c.portCode.hasError('taken')) { <mat-error>This code is already in use</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Port name</mat-label>
          <input matInput formControlName="portName" maxlength="100" />
          <mat-error>Name is required</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Address</mat-label>
          <input matInput formControlName="portAddress" maxlength="100" />
          <mat-error>Address is required</mat-error>
        </mat-form-field>
        <div class="row">
        <mat-form-field appearance="outline">
          <mat-label>Latitude</mat-label>
          <input matInput type="number" step="0.0001" formControlName="latitude" />
          <mat-hint>For live tracking</mat-hint>
          <mat-error>-90 to 90</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Longitude</mat-label>
          <input matInput type="number" step="0.0001" formControlName="longitude" />
          <mat-error>-180 to 180</mat-error>
        </mat-form-field>
      </div>
        @if (errors().length) { <ul class="error">@for (m of errors(); track m) { <li>{{ m }}</li> }</ul> }
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button type="submit" [disabled]="saving() || status() === 'PENDING'">
          @if (saving()) { <mat-spinner diameter="20" /> } @else { {{ isEdit ? 'Save changes' : 'Create port' }} }
        </button>
      </mat-dialog-actions>
    </form>
  `,
})
export class PortFormDialog extends DialogBase<Port> {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(FleetApi);
  protected readonly port = inject<Port | null>(MAT_DIALOG_DATA);
  protected readonly isEdit = this.port !== null;
  protected readonly conflictMsg = 'A port with this code already exists.';
  protected readonly notFoundMsg = 'This port no longer exists.';

  protected readonly form = this.fb.group({
  portCode: this.fb.control(
    { value: this.port?.portCode ?? '', disabled: this.isEdit },
    {
      validators: [
        Validators.required,
        Validators.pattern(PORT_CODE),
      ],
      asyncValidators: this.isEdit
        ? []
        : [uniqueValue(
            (v) => this.api.portExists(v),
            PORT_CODE
          )],
    },
  ),

  portName: [
    this.port?.portName ?? '',
    [
      Validators.required,
      Validators.maxLength(100),
    ],
  ],

  portAddress: [
    this.port?.portAddress ?? '',
    [
      Validators.required,
      Validators.maxLength(100),
    ],
  ],

  latitude: this.fb.control<number | null>(
    this.port?.latitude ?? null,
    [
      Validators.min(-90),
      Validators.max(90),
    ],
  ),

  longitude: this.fb.control<number | null>(
    this.port?.longitude ?? null,
    [
      Validators.min(-180),
      Validators.max(180),
    ],
  ),
});

protected readonly c = this.form.controls;
  protected readonly status = toSignal(this.form.statusChanges, { initialValue: this.form.status });

  protected upper(): void {
    const ctrl = this.c.portCode;
    if (ctrl.value !== ctrl.value.toUpperCase()) ctrl.setValue(ctrl.value.toUpperCase());
  }

  protected submit(): void {
    if (this.saving() || this.form.pending) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const body: PortRequest = {
  portCode: v.portCode.trim(),
  portName: v.portName.trim(),
  portAddress: v.portAddress.trim(),
  latitude: v.latitude,
  longitude: v.longitude,
};
    this.run(this.port ? this.api.updatePort(this.port.portCode, body) : this.api.createPort(body));
  }
}

// ================= SHIP =================
@Component({
  selector: 'app-ship-form-dialog',
  imports: [...COMMON, MatSelectModule],
  styles: DIALOG_STYLES,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit ship' : 'New ship' }}</h2>
    <form [formGroup]="form" (ngSubmit)="submit()">
      <mat-dialog-content class="content">
        <mat-form-field appearance="outline">
          <mat-label>Ship code</mat-label>
          <input matInput formControlName="shipCode" maxlength="7" (input)="upper()" />
          <mat-hint>{{ status() === 'PENDING' ? 'Checking availability…' : 'Format: SH12345' }}</mat-hint>
          @if (c.shipCode.hasError('required')) { <mat-error>Code is required</mat-error> }
          @else if (c.shipCode.hasError('pattern')) { <mat-error>Use SH followed by 5 digits</mat-error> }
          @else if (c.shipCode.hasError('taken')) { <mat-error>This code is already in use</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Company name</mat-label>
          <input matInput formControlName="shipCompanyName" maxlength="50" />
          <mat-error>Company name is required (max 50)</mat-error>
        </mat-form-field>
        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>Capacity (containers)</mat-label>
            <input matInput type="number" min="1" formControlName="shipCapacity" />
            <mat-error>Whole number, 1 or more</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Home port</mat-label>
            <mat-select formControlName="portCode">
              @for (p of lookup.ports(); track p.portId) {
                <mat-option [value]="p.portCode">{{ p.portName }} ({{ p.portCode }})</mat-option>
              }
            </mat-select>
            @if (lookup.ports().length === 0) { <mat-hint>Create a port first</mat-hint> }
            <mat-error>Pick a port</mat-error>
          </mat-form-field>
        </div>
        @if (errors().length) { <ul class="error">@for (m of errors(); track m) { <li>{{ m }}</li> }</ul> }
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button type="submit" [disabled]="saving() || status() === 'PENDING'">
          @if (saving()) { <mat-spinner diameter="20" /> } @else { {{ isEdit ? 'Save changes' : 'Create ship' }} }
        </button>
      </mat-dialog-actions>
    </form>
  `,
})
export class ShipFormDialog extends DialogBase<Ship> {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(FleetApi);
  protected readonly lookup = inject(FleetLookup);
  protected readonly ship = inject<Ship | null>(MAT_DIALOG_DATA);
  protected readonly isEdit = this.ship !== null;
  protected readonly conflictMsg = 'A ship with this code already exists.';
  protected readonly notFoundMsg = 'The ship or the selected port no longer exists.';

  protected readonly form = this.fb.group({
    shipCode: this.fb.control(
      { value: this.ship?.shipCode ?? '', disabled: this.isEdit },
      {
        validators: [Validators.required, Validators.pattern(SHIP_CODE)],
        asyncValidators: this.isEdit ? [] : [uniqueValue((v) => this.api.shipExists(v), SHIP_CODE)],
      },
    ),
    shipCompanyName: [this.ship?.shipCompanyName ?? '', [Validators.required, Validators.maxLength(50)]],
    shipCapacity: this.fb.control<number | null>(this.ship?.shipCapacity ?? null, [
      Validators.required, Validators.min(1), Validators.max(1_000_000), Validators.pattern(/^\d+$/),
    ]),
    portCode: [this.ship?.port.portCode ?? '', Validators.required],
  });
  protected readonly c = this.form.controls;
  protected readonly status = toSignal(this.form.statusChanges, { initialValue: this.form.status });

  constructor() {
    super();
    this.lookup.load();
  }

  protected upper(): void {
    const ctrl = this.c.shipCode;
    if (ctrl.value !== ctrl.value.toUpperCase()) ctrl.setValue(ctrl.value.toUpperCase());
  }

  protected submit(): void {
    if (this.saving() || this.form.pending) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const body: ShipRequest = {
      shipCode: v.shipCode.trim(), shipCompanyName: v.shipCompanyName.trim(),
      shipCapacity: Number(v.shipCapacity), portCode: v.portCode,
    };
    this.run(this.ship ? this.api.updateShip(this.ship.shipCode, body) : this.api.createShip(body));
  }
}

// ================= VEHICLE =================
@Component({
  selector: 'app-vehicle-form-dialog',
  imports: [...COMMON, MatSelectModule],
  styles: DIALOG_STYLES,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit vehicle' : 'New vehicle' }}</h2>
    <form [formGroup]="form" (ngSubmit)="submit()">
      <mat-dialog-content class="content">
        <mat-form-field appearance="outline">
          <mat-label>Vehicle number</mat-label>
          <input matInput formControlName="vehicleNumber" maxlength="20" (input)="upper()" />
          <mat-hint>{{ status() === 'PENDING' ? 'Checking availability…' : 'e.g. GJ01AB1234' }}</mat-hint>
          @if (c.vehicleNumber.hasError('required')) { <mat-error>Number is required</mat-error> }
          @else if (c.vehicleNumber.hasError('pattern')) { <mat-error>4-20 letters, digits or dashes</mat-error> }
          @else if (c.vehicleNumber.hasError('taken')) { <mat-error>This number is already registered</mat-error> }
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Name</mat-label>
          <input matInput formControlName="vehicleName" maxlength="20" />
          <mat-error>Name is required (max 20)</mat-error>
        </mat-form-field>
        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>Type</mat-label>
            <mat-select formControlName="vehicleType">
              @for (t of types; track t.value) { <mat-option [value]="t.value">{{ t.label }}</mat-option> }
            </mat-select>
            <mat-error>Pick a type</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Capacity</mat-label>
            <input matInput type="number" min="1" formControlName="vehicleCapacity" />
            <mat-error>Whole number, 1 or more</mat-error>
          </mat-form-field>
        </div>
        @if (errors().length) { <ul class="error">@for (m of errors(); track m) { <li>{{ m }}</li> }</ul> }
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-button type="button" mat-dialog-close>Cancel</button>
        <button mat-flat-button type="submit" [disabled]="saving() || status() === 'PENDING'">
          @if (saving()) { <mat-spinner diameter="20" /> } @else { {{ isEdit ? 'Save changes' : 'Create vehicle' }} }
        </button>
      </mat-dialog-actions>
    </form>
  `,
})
export class VehicleFormDialog extends DialogBase<Vehicle> {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(FleetApi);
  protected readonly vehicle = inject<Vehicle | null>(MAT_DIALOG_DATA);
  protected readonly isEdit = this.vehicle !== null;
  protected readonly types = VEHICLE_TYPES;
  protected readonly conflictMsg = 'A vehicle with this number already exists.';
  protected readonly notFoundMsg = 'This vehicle no longer exists.';

  protected readonly form = this.fb.group({
    vehicleNumber: this.fb.control(
      { value: this.vehicle?.vehicleNumber ?? '', disabled: this.isEdit },
      {
        validators: [Validators.required, Validators.pattern(VEHICLE_NO)],
        asyncValidators: this.isEdit ? [] : [uniqueValue((v) => this.api.vehicleExists(v), VEHICLE_NO)],
      },
    ),
    vehicleName: [this.vehicle?.vehicleName ?? '', [Validators.required, Validators.maxLength(20)]],
    vehicleType: this.fb.control<Vehicle['vehicleType'] | ''>(this.vehicle?.vehicleType ?? '', Validators.required),
    vehicleCapacity: this.fb.control<number | null>(this.vehicle?.vehicleCapacity ?? null, [
      Validators.required, Validators.min(1), Validators.max(1_000_000), Validators.pattern(/^\d+$/),
    ]),
  });
  protected readonly c = this.form.controls;
  protected readonly status = toSignal(this.form.statusChanges, { initialValue: this.form.status });

  protected upper(): void {
    const ctrl = this.c.vehicleNumber;
    if (ctrl.value !== ctrl.value.toUpperCase()) ctrl.setValue(ctrl.value.toUpperCase());
  }

  protected submit(): void {
    if (this.saving() || this.form.pending) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const body: VehicleRequest = {
      vehicleNumber: v.vehicleNumber.trim(), vehicleName: v.vehicleName.trim(),
      vehicleType: v.vehicleType as VehicleRequest['vehicleType'], vehicleCapacity: Number(v.vehicleCapacity),
    };
    this.run(this.vehicle ? this.api.updateVehicle(this.vehicle.vehicleNumber, body) : this.api.createVehicle(body));
  }
}