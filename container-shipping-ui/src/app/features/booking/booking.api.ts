import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../../core/auth/auth.models';
import { SKIP_ERROR_TOAST } from '../../core/interceptors/error-interceptor';
import {
  Booking, BookingQuery, BookingRequest, BookingSummary, Container, TimelineEvent,
} from './booking.models';
import { Auth } from '../../core/auth/auth';

const silent = () => new HttpContext().set(SKIP_ERROR_TOAST, true);

@Injectable({ providedIn: 'root' })
export class BookingApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/bookings`;
private readonly auth = inject(Auth);
  list(q: BookingQuery): Observable<PagedResponse<Booking>> {
  let params = new HttpParams().set('page', q.page).set('size', q.size).set('sort', 'createdAt,desc');
  if (q.status) params = params.set('status', q.status);
  const url = this.auth.isAdmin() ? `${environment.apiUrl}/api/staff/bookings` : `${this.base}/my`;
  return this.http.get<PagedResponse<Booking>>(url, { params });
}
  get(id: number) { return this.http.get<Booking>(`${this.base}/${id}`, { context: silent() }); }
  create(body: BookingRequest) { return this.http.post<Booking>(this.base, body, { context: silent() }); }
  cancel(id: number) { return this.http.post<Booking>(`${this.base}/${id}/cancel`, {}, { context: silent() }); }
  timeline(id: number) {
    return this.http.get<TimelineEvent[]>(`${this.base}/${id}/timeline`, { context: silent() });
  }
  summary() { return this.http.get<BookingSummary>(`${this.base}/summary`, { context: silent() }); }

  availableContainers(): Observable<Container[]> {
    return this.http
      .get<PagedResponse<Container>>(`${environment.apiUrl}/api/containers/status/AVAILABLE`, {
        params: new HttpParams().set('size', 100),
      })
      .pipe(map((r) => r.content));
  }
}