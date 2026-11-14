import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { forkJoin, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../../core/auth/auth.models';
import { SKIP_ERROR_TOAST } from '../../core/interceptors/error-interceptor';
import { Container, ContainerStatus, ContainerType } from '../booking/booking.models';
import { ListQuery } from '../fleet/fleet.model';

export interface ContainerRequest {
  containerType: ContainerType;
  containerSize: number;
  containerCompanyName: string;
}

const silent = () => new HttpContext().set(SKIP_ERROR_TOAST, true);

@Injectable({ providedIn: 'root' })
export class ContainerApi {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/containers`;

  /** `query` carries the status filter here ('' = all). */
  list(q: ListQuery): Observable<PagedResponse<Container>> {
    const params = new HttpParams().set('page', q.page).set('size', q.size).set('sort', q.sort);
    const url = q.query ? `${this.base}/status/${q.query}` : this.base;
    return this.http.get<PagedResponse<Container>>(url, { params });
  }

  /** Creates `count` identical containers (handy for stocking inventory). */
  create(body: ContainerRequest, count: number): Observable<Container[]> {
    return forkJoin(
      Array.from({ length: count }, () => this.http.post<Container>(this.base, body, { context: silent() })),
    );
  }

  setStatus(id: number, status: ContainerStatus): Observable<Container> {
    return this.http.patch<Container>(`${this.base}/${id}/status`, { status }, { context: silent() });
  }
}