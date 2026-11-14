import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { UserResponse } from '../auth/auth.models';

export type KycStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';
export type DocumentType = 'AADHAR' | 'PAN' | 'PASSPORT' | 'DRIVING_LICENSE' | 'VOTER_ID';

export interface Address {
  addressId: number; street: string; city: string; state: string;
  country: string; pincode: string; createdAt: string; updatedAt: string;
}
export type AddressInput = Omit<Address, 'addressId' | 'createdAt' | 'updatedAt'>;

export interface KycDocument {
  documentId: number; documentType: DocumentType; documentNumber: string; createdAt: string;
}
export interface KycSummary { status: KycStatus; documents: KycDocument[]; }
export interface DriverSummary {
  status: 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED';
  driverType: 'VEHICLE_DRIVER' | 'SHIP_DRIVER'; rejectionReason: string | null;
}
export interface Profile {
  user: UserResponse; addresses: Address[]; kyc: KycSummary | null; driver: DriverSummary | null;
}
export interface Eligibility {
  eligible: boolean; hasAddress: boolean; hasKycDocument: boolean;
  kycStatus: KycStatus | null; missing: ('ADDRESS' | 'KYC_DOCUMENT')[];
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/users/me`;

  profile() { return this.http.get<Profile>(`${this.base}/profile`); }
  eligibility() { return this.http.get<Eligibility>(`${this.base}/eligibility`); }

  addAddress(b: AddressInput) { return this.http.post<Address>(`${this.base}/addresses`, b); }
  updateAddress(id: number, b: AddressInput) { return this.http.put<Address>(`${this.base}/addresses/${id}`, b); }
  deleteAddress(id: number) { return this.http.delete<void>(`${this.base}/addresses/${id}`); }

  addKyc(b: { documentType: DocumentType; documentNumber: string }) {
    return this.http.post<KycSummary>(`${this.base}/kyc/documents`, b);
  }
  deleteKyc(id: number) { return this.http.delete<KycSummary>(`${this.base}/kyc/documents/${id}`); }
}