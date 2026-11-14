import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../auth/auth.models';
import { SKIP_ERROR_TOAST } from '../interceptors/error-interceptor';

export type ApplicantStatus = 'APPLIED' | 'SHORTLISTED' | 'HIRED' | 'REJECTED';
export interface Job { jobId: number; title: string; posting: string; description: string; portId: number | null;
  status: 'OPEN' | 'CLOSED'; createdAt: string; }
export interface JobApp { applicationId: number; jobId: number; jobTitle: string; posting: string; userId: number;
  userName: string; userEmail: string; userPhone: string; resumeName: string; status: ApplicantStatus;
  onboardingPortId: number | null; createdAt: string; joiningDate: string | null; }

const silent = () => new HttpContext().set(SKIP_ERROR_TOAST, true);

@Injectable({ providedIn: 'root' })
export class CareersService {
  private readonly http = inject(HttpClient);
  private readonly pub = `${environment.apiUrl}/api/careers`;
  private readonly adm = `${environment.apiUrl}/api/admin/careers`;

  jobs() { return this.http.get<Job[]>(`${this.pub}/jobs`); }
  mine() { return this.http.get<JobApp[]>(`${this.pub}/my`); }
  apply(jobId: number, file: File) {
    const f = new FormData(); f.append('file', file);
    return this.http.post<JobApp>(`${this.pub}/jobs/${jobId}/apply`, f, { context: silent() });
  }
  // admin
  allJobs() { return this.http.get<Job[]>(`${this.adm}/jobs`); }
  createJob(b: { title: string; posting: string; description: string; portId: number | null }) {
    return this.http.post<Job>(`${this.adm}/jobs`, b, { context: silent() });
  }
  setJobStatus(id: number, status: 'OPEN' | 'CLOSED') {
    return this.http.put<Job>(`${this.adm}/jobs/${id}/status`, { status }, { context: silent() });
  }
  applications(status: ApplicantStatus | '') {
    let params = new HttpParams().set('size', 50);
    if (status) params = params.set('status', status);
    return this.http.get<PagedResponse<JobApp>>(`${this.adm}/applications`, { params });
  }
  resume(id: number) { return this.http.get(`${this.adm}/applications/${id}/resume`, { responseType: 'blob' }); }
  interview(id: number) { return this.http.post<JobApp>(`${this.adm}/applications/${id}/interview`, {}, { context: silent() }); }
  hire(id: number, portId: number, portName: string) {
    return this.http.post<JobApp>(`${this.adm}/applications/${id}/hire`, { portId, portName }, { context: silent() });
  }
  reject(id: number) { return this.http.post<JobApp>(`${this.adm}/applications/${id}/reject`, {}, { context: silent() }); }
  setJoiningDate(id: number, joiningDate: string, onboardingPortId: number, portName: string) {
    return this.http.post<JobApp>(`${this.adm}/applications/${id}/joining-date`,
      { joiningDate, onboardingPortId, portName }, { context: silent() });
  }
}