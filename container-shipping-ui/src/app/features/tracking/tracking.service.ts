import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Client, StompSubscription } from '@stomp/stompjs';
import { catchError, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TokenStorage } from '../../core/auth/token-storage';
import { SKIP_ERROR_TOAST } from '../../core/interceptors/error-interceptor';
import { ConnectionStatus, GpsPosition, TrackedContainer } from './tracking.models';

const silent = () => new HttpContext().set(SKIP_ERROR_TOAST, true);

@Injectable({ providedIn: 'root' })
export class TrackingService {
  private readonly http = inject(HttpClient);
  private readonly storage = inject(TokenStorage);

  private client?: Client;
  private readonly subs = new Map<number, StompSubscription>();
  private wanted = new Set<number>();

  private readonly _status = signal<ConnectionStatus>('offline');
  readonly status = this._status.asReadonly();
  readonly positions = signal<Record<number, GpsPosition>>({});

  tracked() {
    return this.http.get<TrackedContainer[]>(`${environment.apiUrl}/api/bookings/tracking/containers`);
  }

  connect(): void {
    if (this.client) return;
    this._status.set('connecting');
    this.client = new Client({
      brokerURL: environment.wsUrl,
      reconnectDelay: 3000,
      beforeConnect: (c) => {
        c.connectHeaders = { Authorization: `Bearer ${this.storage.accessToken ?? ''}` };
      },
      onConnect: () => {
        this._status.set('connected');
        this.subs.clear();
        this.wanted.forEach((id) => this.subscribe(id));
      },
      onWebSocketClose: () => this._status.set('reconnecting'),
      onStompError: () => this._status.set('reconnecting'),
    });
    this.client.activate();
  }

  /** Subscribe to exactly these containers and load their last known position. */
  track(ids: number[]): void {
    this.wanted = new Set(ids);
    for (const [id, sub] of this.subs) {
      if (!this.wanted.has(id)) { sub.unsubscribe(); this.subs.delete(id); }
    }
    ids.forEach((id) => {
      this.subscribe(id);
      this.http.get<GpsPosition>(`${environment.apiUrl}/api/locations/container/${id}/latest`,
        { context: silent() })
        .pipe(catchError(() => of(null)))
        .subscribe((p) => { if (p) this.put(p); });
    });
  }

  disconnect(): void {
    this.subs.clear();
    this.wanted.clear();
    this.client?.deactivate();
    this.client = undefined;
    this._status.set('offline');
    this.positions.set({});
  }

  private subscribe(id: number): void {
    if (!this.client?.connected || this.subs.has(id)) return;
    this.subs.set(id, this.client.subscribe(`/topic/container/${id}`,
      (m) => this.put(JSON.parse(m.body) as GpsPosition)));
  }

  private put(p: GpsPosition): void {
    this.positions.update((s) => ({ ...s, [p.containerId]: p }));
  }
}