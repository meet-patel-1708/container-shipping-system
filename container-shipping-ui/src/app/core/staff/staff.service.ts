import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PagedResponse, Role } from '../auth/auth.models';

export interface Staff {
  userId: number; name: string; email: string; phone: string;
  role: Role; emailVerified: boolean; posting: string | null;assignedPortId: number | null;createdAt: string;
}
export interface InviteStaff { name: string; email: string; phone: string; role: Role; posting?: string;assignedPortId?: number | null}

@Injectable({ providedIn: 'root' })
export class StaffService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/admin/staff`;

  list(page: number) {
    return this.http.get<PagedResponse<Staff>>(this.base, {
      params: new HttpParams().set('page', page).set('size', 10),
    });
  }
  invite(body: InviteStaff) { return this.http.post<Staff>(this.base, body); }
  resend(userId: number) { return this.http.post<void>(`${this.base}/${userId}/resend`, null); }
}