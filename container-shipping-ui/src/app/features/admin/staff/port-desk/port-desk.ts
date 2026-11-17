import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { Auth } from '../../../../core/auth/auth';
import { StaffApi } from '../../../../core/staff/staff-api';
import { Booking } from '../../../booking/booking.models';
import { FleetLookup } from '../../../fleet/fleet-lookup';

@Component({
  selector: 'app-port-desk',
  imports: [DatePipe, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
:host {
  display: block;
  min-height: 100%;
  font-family: 'Inter', sans-serif;
  color: #172554;
}

.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px;
  animation: pageEnter 0.5s ease both;
}

/* Main heading */
h2 {
  color: #172554;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -1px;
  margin: 0 0 30px;
}

/* Section headings */
h3 {
  color: #1e3a8a;
  font-size: 20px;
  font-weight: 750;
  margin: 35px 0 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

h3::before {
  content: '';
  width: 5px;
  height: 23px;
  border-radius: 10px;
  background: linear-gradient(180deg, #6366f1, #06b6d4);
}

/* Section description */
.page > p {
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
  margin-bottom: 18px;
}

/* Booking cards */
.card {
  position: relative;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 22px;
  margin-bottom: 18px;
  box-shadow: 0 4px 15px rgba(15, 23, 42, 0.035);
  transition: all 0.3s ease;
  animation: cardEnter 0.5s ease both;
}

.card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 0;
  background: linear-gradient(180deg, #6366f1, #06b6d4);
  transition: height 0.3s ease;
}

.card:hover {
  transform: translateY(-4px);
  border-color: #c7d2fe;
  box-shadow: 0 12px 30px rgba(79, 70, 229, 0.09);
}

.card:hover::before {
  height: 100%;
}

/* Booking information */
.card > strong {
  color: #172554;
  font-size: 17px;
  font-weight: 750;
}

.card small {
  color: #64748b;
  font-size: 12px;
  line-height: 1.8;
}

/* Product list */
.card ul {
  list-style: none;
  padding: 12px 15px;
  margin: 15px 0;
  background: #f8fafc;
  border: 1px solid #e8edf5;
  border-radius: 10px;
}

.card li {
  position: relative;
  color: #475569;
  font-size: 13px;
  padding: 7px 5px 7px 22px;
  border-bottom: 1px solid #edf1f7;
}

.card li:last-child {
  border-bottom: none;
}

.card li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #6366f1;
  font-weight: 800;
}

/* Input rows */
.row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 16px;
}

/* Input fields */
.row input[type="text"],
.row input[type="number"] {
  min-height: 40px;
  box-sizing: border-box;
  padding: 9px 12px;
  background: #f8fafc;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  color: #172554;
  font-family: inherit;
  font-size: 13px;
  outline: none;
  transition: all 0.25s ease;
}

.row input[type="text"]:focus,
.row input[type="number"]:focus {
  background: #ffffff;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.row input::placeholder {
  color: #94a3b8;
}

/* Checkbox labels */
.row label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #475569;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.row label:hover {
  background: #eef2ff;
  border-color: #c7d2fe;
}

.row input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #4f46e5;
  cursor: pointer;
}

/* Buttons */
.card button[mat-flat-button] {
  min-height: 40px;
  padding: 0 18px;
  border-radius: 9px;
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: white;
  font-size: 12px;
  font-weight: 650;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.18);
  transition: all 0.25s ease;
}

.card button[mat-flat-button]:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 7px 17px rgba(79, 70, 229, 0.3);
}

.card button[mat-stroked-button] {
  min-height: 40px;
  padding: 0 16px;
  border-radius: 9px;
  border-color: #c7d2fe;
  color: #4f46e5;
  font-size: 12px;
  font-weight: 650;
  transition: all 0.25s ease;
}

.card button[mat-stroked-button]:hover {
  background: #eef2ff;
  border-color: #818cf8;
}

/* Disabled buttons */
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Refresh button */
.page > button[mat-button] {
  margin-top: 12px;
  color: #4f46e5;
  font-weight: 650;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.page > button[mat-button]:hover {
  background: #eef2ff;
}

/* Error and success messages */
.error,
.ok {
  padding: 13px 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  animation: messageEnter 0.3s ease both;
}

.error {
  color: #b91c1c;
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.ok {
  color: #047857;
  background: #ecfdf5;
  border: 1px solid #a7f3d0;
}

/* Empty states */
.page > p:has(small) {
  color: #64748b;
}

/* Animations */
@keyframes pageEnter {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes messageEnter {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .page {
    padding: 20px 14px;
  }

  h2 {
    font-size: 25px;
  }

  h3 {
    font-size: 18px;
  }

  .card {
    padding: 17px;
  }

  .row {
    align-items: stretch;
    gap: 9px;
  }

  .row input[type="text"],
  .row input[type="number"] {
    width: 100%;
  }

  .row button {
    max-width: 100%;
  }

  .row label {
    width: 100%;
    box-sizing: border-box;
  }
}

/* Accessibility */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
`],
  template: `
    <div class="page">
      <h2>Port desk</h2>
      @if (error(); as m) { <p class="error">{{ m }}</p> }
      @if (info(); as m) { <p class="ok">{{ m }}</p> }

      <h3>Goods arriving at the port</h3>
      <p><small>Send the customer an SMS code, inspect the goods, then accept with the code or reject with a reason.</small></p>
      @for (b of incoming(); track b.bookingId) {
        <div class="card">
          <strong>Booking #{{ b.bookingId }}</strong> · container #{{ b.containerId }} ·
          {{ fleet.portName(b.originPortId) }} → {{ fleet.portName(b.destPortId) }}<br />
          <small>Departs {{ b.scheduleAt | date: 'medium' }} · declared weight {{ b.totalWeightKg }} kg</small>
          <ul>@for (i of b.items; track i.itemId) { <li>{{ i.productName ?? ('Product #' + i.productId) }} × {{ i.quantity }}</li> }</ul>
          @if (due(b)) {
            <div class="row">
              <button mat-stroked-button type="button" (click)="sendCode(b, false)">Send code to customer (SMS)</button>
            </div>
            <div class="row">
              <label><input type="checkbox" #pk checked /> Packaging valid</label>
              <label><input type="checkbox" #lb checked /> Label and documents valid</label>
              <label>Actual weight (kg) <input type="number" #wt [value]="b.totalWeightKg" step="0.01" min="0.01" /></label>
            </div>
            <div class="row">
              <input type="text" #cd maxlength="6" placeholder="6-digit code" />
              <input type="text" #nt placeholder="Note, or the rejection reason" size="32" />
              <button mat-flat-button type="button" (click)="accept(b, pk.checked, lb.checked, wt.value, cd.value, nt.value)">Accept goods</button>
              <button mat-stroked-button type="button" (click)="reject(b, pk.checked, lb.checked, wt.value, nt.value)">Reject goods</button>
            </div>
          } @else {
            <small>Goods are received from {{ b.scheduleAt | date: 'mediumDate' }}.</small>
          }
        </div>
      } @empty { <p>No goods are waiting for this port.</p> }

      <h3>Deliveries at this port</h3>
      <p><small>The container has arrived. Send the customer a code and enter it when they collect their goods.</small></p>
      @for (b of deliveries(); track b.bookingId) {
        <div class="card">
          <strong>Booking #{{ b.bookingId }}</strong> · container #{{ b.containerId }} · {{ b.totalWeightKg }} kg
          <ul>@for (i of b.items; track i.itemId) { <li>{{ i.productName ?? ('Product #' + i.productId) }} × {{ i.quantity }}</li> }</ul>
          <div class="row">
            <button mat-stroked-button type="button" (click)="sendCode(b, true)">Send code to customer (SMS)</button>
            <input type="text" #dc maxlength="6" placeholder="6-digit code" />
            <button mat-flat-button type="button" (click)="deliver(b, dc.value)">Hand over goods</button>
          </div>
        </div>
      } @empty { <p>No deliveries waiting.</p> }
      <button mat-button type="button" (click)="load()">Refresh</button>
    </div>`,
})
export class PortDesk {
  private readonly api = inject(StaffApi);
  private readonly auth = inject(Auth);
  protected readonly fleet = inject(FleetLookup);
  protected readonly all = signal<Booking[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly info = signal<string | null>(null);
  private readonly mine = computed(() => this.auth.user()?.assignedPortId ?? null);

  private ok(port: number): boolean { return this.auth.isAdmin() || this.mine() === port; }
  protected readonly incoming = computed(() => this.all()
    .filter((b) => b.status === 'CONFIRMED' && this.ok(b.originPortId))
    .sort((a, b) => a.scheduleAt.localeCompare(b.scheduleAt)));
  protected readonly deliveries = computed(() => this.all().filter((b) => b.status === 'ARRIVED' && this.ok(b.destPortId)));

  constructor() { this.fleet.load(); this.load(); }

  protected load(): void {
    this.api.bookings().subscribe({ next: (b) => this.all.set(b), error: (e) => this.fail(e) });
  }
  protected due(b: Booking): boolean {
    const d = new Date(); const t = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    return b.scheduleAt.slice(0, 10) <= t;
  }
  protected sendCode(b: Booking, delivery: boolean): void {
    this.clear();
    this.api.sendCode(b.bookingId, delivery).subscribe({
      next: () => this.info.set(`Code sent to the customer of booking #${b.bookingId}.`), error: (e) => this.fail(e) });
  }
  protected accept(b: Booking, pk: boolean, lb: boolean, wt: string, otp: string, note: string): void {
    this.clear();
    if (!/^\d{6}$/.test(otp.trim())) { this.error.set('Enter the 6-digit code.'); return; }
    this.api.receive(b.bookingId, { otp: otp.trim(), packagingOk: pk, labelOk: lb, actualWeightKg: Number(wt), note }).subscribe({
      next: () => { this.info.set(`Booking #${b.bookingId} received at the port.`); this.load(); }, error: (e) => this.fail(e) });
  }
  protected reject(b: Booking, pk: boolean, lb: boolean, wt: string, reason: string): void {
    this.clear();
    if (!reason.trim()) { this.error.set('Write the rejection reason in the note box.'); return; }
    if (!confirm(`Reject booking #${b.bookingId}? The customer is notified and the payment is marked refunded.`)) return;
    this.api.rejectGoods(b.bookingId, { packagingOk: pk, labelOk: lb, actualWeightKg: Number(wt), reason: reason.trim() }).subscribe({
      next: () => { this.info.set(`Booking #${b.bookingId} rejected.`); this.load(); }, error: (e) => this.fail(e) });
  }
  protected deliver(b: Booking, otp: string): void {
    this.clear();
    if (!/^\d{6}$/.test(otp.trim())) { this.error.set('Enter the 6-digit code.'); return; }
    this.api.deliver(b.bookingId, otp.trim()).subscribe({
      next: () => { this.info.set(`Booking #${b.bookingId} delivered.`); this.load(); }, error: (e) => this.fail(e) });
  }
  private clear(): void { this.error.set(null); this.info.set(null); }
  private fail(e: unknown): void {
    const x = e as HttpErrorResponse;
    this.error.set(x.status === 0 ? 'Cannot reach the server.' : x.error?.message ?? 'Something went wrong.');
  }
}
