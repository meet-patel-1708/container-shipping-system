import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../auth/auth.models';
import { DocumentType, KycStatus } from '../profile/profile.service';

export interface AdminKycDocument {
  documentId: number;
  documentType: DocumentType;
  documentNumber: string;
}

export interface AdminKyc {
  kycId: number;
  userId: number;
  userName: string;
  userEmail: string;
  status: KycStatus;
  documents: AdminKycDocument[];
}

@Injectable({ providedIn: 'root' })
export class AdminKycService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/admin/kyc`;

  list(status: KycStatus | undefined, page: number) {
    let params = new HttpParams().set('page', page).set('size', 10);
    if (status) params = params.set('status', status);
    return this.http.get<PagedResponse<AdminKyc>>(this.base, { params });
  }

  setStatus(kycId: number, status: KycStatus) {
    return this.http.put<void>(`${this.base}/${kycId}/status`, { status });
  }
}