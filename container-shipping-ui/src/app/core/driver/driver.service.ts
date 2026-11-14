import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../auth/auth.models';
import {
  ApplicationStatus, DriverApplication, DriverApplicationRequest, DriverDocType,
} from './driver.model';

@Injectable({ providedIn: 'root' })
export class DriverService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/driver-applications`;
  private readonly admin = `${environment.apiUrl}/api/admin/driver-applications`;

  // ---- applicant
  mine(): Observable<DriverApplication | null> {
    return this.http.get<DriverApplication | null>(`${this.base}/me`); // 204 -> null
  }

  saveDraft(body: DriverApplicationRequest): Observable<DriverApplication> {
    return this.http.put<DriverApplication>(`${this.base}/me`, body);
  }

  upload(type: DriverDocType, file: File): Observable<DriverApplication> {
    const form = new FormData();
    form.append('type', type);
    form.append('file', file);
    // no Content-Type header: the browser adds the multipart boundary
    return this.http.post<DriverApplication>(`${this.base}/me/documents`, form);
  }

  submit(): Observable<DriverApplication> {
    return this.http.post<DriverApplication>(`${this.base}/me/submit`, null);
  }

  // ---- admin
  list(status: ApplicationStatus | undefined, page: number, size = 10) {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<PagedResponse<DriverApplication>>(this.admin, { params });
  }

  approve(id: number): Observable<DriverApplication> {
    return this.http.post<DriverApplication>(`${this.admin}/${id}/approve`, null);
  }

  reject(id: number, reason: string): Observable<DriverApplication> {
    return this.http.post<DriverApplication>(`${this.admin}/${id}/reject`, { reason });
  }

  adminFile(documentId: number): Observable<Blob> {
    return this.http.get(`${this.admin}/documents/${documentId}/file`, { responseType: 'blob' });
  }
}