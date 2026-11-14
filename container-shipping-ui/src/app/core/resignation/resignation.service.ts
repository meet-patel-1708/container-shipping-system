import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../auth/auth.models';
import { SKIP_ERROR_TOAST } from '../interceptors/error-interceptor';

export type ResignationStatus = 'REQUESTED' | 'NOTICE' | 'WITHDRAWN' | 'REJECTED' | 'COMPLETED';
export interface Resignation {
  resignationId: number; userId: number; userName: string; userEmail: string; posting: string | null;
  assignedPortId: number | null; reason: string; status: ResignationStatus; noticeDays: number | null;
  lastWorkingDate: string | null; requestedAt: string;
}
const silent = () => new HttpContext().set(SKIP_ERROR_TOAST, true);

@Injectable({ providedIn: 'root' })
export class ResignationService {
  private readonly http = inject(HttpClient);
  private readonly me = `${environment.apiUrl}/api/resignations`;
  private readonly adm = `${environment.apiUrl}/api/admin/resignations`;

  mine() { return this.http.get<Resignation | null>(`${this.me}/me`, { context: silent() }); }
  request(reason: string) { return this.http.post<Resignation>(this.me, { reason }, { context: silent() }); }
  withdraw() { return this.http.delete<Resignation>(`${this.me}/me`, { context: silent() }); }

  list(status: ResignationStatus | '') {
    let params = new HttpParams().set('size', 50);
    if (status) params = params.set('status', status);
    return this.http.get<PagedResponse<Resignation>>(this.adm, { params });
  }
  setNotice(id: number, noticeDays: number) {
    return this.http.put<Resignation>(`${this.adm}/${id}/notice`, { noticeDays }, { context: silent() });
  }
  reject(id: number) { return this.http.post<Resignation>(`${this.adm}/${id}/reject`, {}, { context: silent() }); }
}