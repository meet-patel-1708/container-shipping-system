import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../auth/auth.models';
import { SKIP_ERROR_TOAST } from '../interceptors/error-interceptor';
import { Booking, BookingStatus, ContainerLoad, MovementResult } from '../../features/booking/booking.models';

const silent = () => new HttpContext().set(SKIP_ERROR_TOAST, true);

export interface SimulateBody {
  containerId: number; fromLat: number; fromLng: number; toLat: number; toLng: number;
  steps?: number; intervalMs?: number;
}

@Injectable({ providedIn: 'root' })
export class StaffApi {
  private readonly http = inject(HttpClient);
  private readonly root = environment.apiUrl;

  bookings() {
    return this.http.get<PagedResponse<Booking>>(`${this.root}/api/staff/bookings`, {
      params: new HttpParams().set('size', 200).set('sort', 'createdAt,desc'),
      context: silent(),
    }).pipe(map((r) => r.content));
  }

  advance(containerId: number, status: BookingStatus, note: string) {
    return this.http.post<MovementResult>(`${this.root}/api/staff/containers/${containerId}/advance`,
      { status, note }, { context: silent() });
  }

  simulate(b: SimulateBody) {
    return this.http.post<void>(`${this.root}/api/locations/simulate`, b, { context: silent() });
  }

  stopSimulation(containerId: number) {
    return this.http.delete<void>(`${this.root}/api/locations/simulate/${containerId}`, { context: silent() });
  }

  ship(containerId: number, bookingIds: number[], note: string) {
    return this.http.post<MovementResult>(`${this.root}/api/staff/containers/${containerId}/ship`,
      { bookingIds, note }, { context: silent() });
  }

  sendCode(bookingId: number, delivery: boolean) {
    return this.http.post<void>(
      `${this.root}/api/staff/bookings/${bookingId}/${delivery ? 'delivery' : 'handover'}-otp`,
      {}, { context: silent() });
  }

  receive(id: number, b: { otp: string; packagingOk: boolean; labelOk: boolean; actualWeightKg: number; note: string }) {
    return this.http.post<void>(`${this.root}/api/staff/bookings/${id}/receive`, b, { context: silent() });
  }

  rejectGoods(id: number, b: { packagingOk: boolean; labelOk: boolean; actualWeightKg: number; reason: string }) {
    return this.http.post<void>(`${this.root}/api/staff/bookings/${id}/reject`, b, { context: silent() });
  }

  deliver(id: number, otp: string) {
    return this.http.post<void>(`${this.root}/api/staff/bookings/${id}/deliver`, { otp }, { context: silent() });
  }

  containerLoad() {
    return this.http.get<ContainerLoad[]>(`${this.root}/api/staff/containers/load`, { context: silent() });
  }
}