import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../../core/auth/auth.models';
import { Auth } from '../../core/auth/auth';
import { SKIP_ERROR_TOAST } from '../../core/interceptors/error-interceptor';
import { CreateOrderResponse, FailRequest, Payment, PaymentMethod, PaymentQuery, VerifyRequest } from './payment.models';

const silent = () => new HttpContext().set(SKIP_ERROR_TOAST, true);

@Injectable({ providedIn: 'root' })
export class PaymentApi {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(Auth);
  private readonly base = `${environment.apiUrl}/api/payments`;

  list(q: PaymentQuery): Observable<PagedResponse<Payment>> {
    let params = new HttpParams().set('page', q.page).set('size', q.size).set('sort', 'createdAt,desc');
    if (q.status) params = params.set('status', q.status);
    const path = this.auth.isAdmin() ? 'all' : 'my';
    return this.http.get<PagedResponse<Payment>>(`${this.base}/${path}`, { params });
  }

  /** 404 is normal right after booking (the payment is created asynchronously via Kafka). */
  byBooking(bookingId: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.base}/booking/${bookingId}`, { context: silent() });
  }

  process(paymentId: number, paymentMethod: PaymentMethod): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/${paymentId}/process`, { paymentMethod }, { context: silent() });
  }
  get(id: number): Observable<Payment> {
    return this.http.get<Payment>(`${this.base}/${id}`, { context: silent() });
  }
  createOrder(id: number): Observable<CreateOrderResponse> {
    return this.http.post<CreateOrderResponse>(`${this.base}/${id}/order`, {}, { context: silent() });
  }
  verify(id: number, body: VerifyRequest): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/${id}/verify`, body, { context: silent() });
  }
  fail(id: number, body: FailRequest): Observable<Payment> {
    return this.http.post<Payment>(`${this.base}/${id}/fail`, body, { context: silent() });
  }
}