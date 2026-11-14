import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PagedResponse } from '../../core/auth/auth.models';
import { SKIP_ERROR_TOAST } from '../../core/interceptors/error-interceptor';
import {
  ListQuery, Port, PortRequest, Ship, ShipRequest, Vehicle, VehicleRequest,
} from './fleet.model';

type RawPage<T> = Omit<PagedResponse<T>, 'totalElements'> & { totalElements?: number; totalElement?: number };

const silent = () => new HttpContext().set(SKIP_ERROR_TOAST, true);

@Injectable({ providedIn: 'root' })
export class FleetApi {
  private readonly http = inject(HttpClient);
  private readonly root = environment.apiUrl;

  // ---- ports ----
  listPorts = (q: ListQuery) => this.list<Port>('/api/ports', q, null);
  createPort = (b: PortRequest) => this.http.post<Port>(`${this.root}/api/ports`, b, { context: silent() });
  updatePort = (code: string, b: PortRequest) =>
    this.http.put<Port>(`${this.root}/api/ports/${encodeURIComponent(code)}`, b, { context: silent() });
  deletePort = (code: string) =>
    this.http.delete<void>(`${this.root}/api/ports/${encodeURIComponent(code)}`, { context: silent() });
  portExists = (code: string) => this.exists(`/api/ports/code/${encodeURIComponent(code)}`);

  // ---- ships ----
  listShips = (q: ListQuery) => this.list<Ship>('/api/ships', q, 'companyName');
  createShip = (b: ShipRequest) => this.http.post<Ship>(`${this.root}/api/ships`, b, { context: silent() });
  updateShip = (code: string, b: ShipRequest) =>
    this.http.put<Ship>(`${this.root}/api/ships/${encodeURIComponent(code)}`, b, { context: silent() });
  deleteShip = (code: string) =>
    this.http.delete<void>(`${this.root}/api/ships/${encodeURIComponent(code)}`, { context: silent() });
  shipExists = (code: string) => this.exists(`/api/ships/${encodeURIComponent(code)}`);

  // ---- vehicles ----
  listVehicles = (q: ListQuery) => this.list<Vehicle>('/api/vehicles', q, 'name');
  createVehicle = (b: VehicleRequest) =>
    this.http.post<Vehicle>(`${this.root}/api/vehicles`, b, { context: silent() });
  updateVehicle = (number: string, b: VehicleRequest) =>
    this.http.put<Vehicle>(`${this.root}/api/vehicles/number/${encodeURIComponent(number)}`, b, { context: silent() });
  deleteVehicle = (number: string) =>
    this.http.delete<void>(`${this.root}/api/vehicles/${encodeURIComponent(number)}`, { context: silent() });
  vehicleExists = (number: string) => this.exists(`/api/vehicles/number/${encodeURIComponent(number)}`);

  /** Uses /search when there is a term and the resource has a search endpoint. */
  private list<T>(base: string, q: ListQuery, searchParam: string | null): Observable<PagedResponse<T>> {
    let params = new HttpParams().set('page', q.page).set('size', q.size);
    if (q.sort) params = params.set('sort', q.sort);
    let path = base;
    const term = q.query.trim();
    if (term && searchParam) {
      path = `${base}/search`;
      params = params.set(searchParam, term);
    }
    return this.http.get<RawPage<T>>(`${this.root}${path}`, { params }).pipe(
      map((r) => ({ ...r, totalElements: r.totalElements ?? r.totalElement ?? 0 }) as PagedResponse<T>),
    );
  }

  /** 200 = taken, 404 = free. Anything else counts as free: the server's 409 is the final check. */
  private exists(path: string): Observable<boolean> {
    return this.http.get(`${this.root}${path}`, { context: silent() }).pipe(
      map(() => true),
      catchError(() => of(false)),
    );
  }
}