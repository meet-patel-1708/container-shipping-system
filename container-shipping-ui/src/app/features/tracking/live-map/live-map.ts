import { DatePipe, DecimalPipe } from '@angular/common';
import {
  afterNextRender, ChangeDetectionStrategy, Component, DestroyRef, effect, ElementRef, inject,
  signal, viewChild,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import * as L from 'leaflet';
import { FleetLookup } from '../../fleet/fleet-lookup';
import { portLatLng, routePoints } from '../../../../port_coords';
import { TrackedContainer } from '../tracking.models';
import { TrackingService } from '../tracking.service';

const DEMO_STEPS = 120;      // points along the route
const DEMO_TICK_MS = 1000;   // one step per second -> about 2 minutes origin to destination

@Component({
  selector: 'app-live-map',
  imports: [DatePipe, DecimalPipe, MatIconModule],
  templateUrl: './live-map.html',
  styleUrl: './live-map.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LiveMap {
  protected readonly tracking = inject(TrackingService);
  protected readonly fleet = inject(FleetLookup);
  protected readonly positions = this.tracking.positions;

  private readonly mapEl = viewChild.required<ElementRef<HTMLDivElement>>('map');
  private map!: L.Map;
  private routes = L.layerGroup();
  private readonly markers = new Map<number, L.Marker>();
  private readonly ready = signal(false);
  private fitted = false;

  /** containerId -> step index of the dummy ship, only for containers with no real GPS signal. */
  private readonly demoStep = new Map<number, number>();
  private demoTimer?: ReturnType<typeof setInterval>;

  protected readonly containers = signal<TrackedContainer[]>([]);
  protected readonly selectedId = signal<number | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
protected noRoute(c: TrackedContainer): boolean { return !this.routeFor(c); }
  constructor() {
    this.fleet.load();

    this.tracking.tracked().subscribe({
      next: (list) => {
        this.containers.set(list);
        this.loading.set(false);
        if (list.length) this.selectedId.set(list[0].containerId);
        this.tracking.connect();
        this.tracking.track(list.map((c) => c.containerId));
      },
      error: () => { this.loading.set(false); this.error.set('Could not load your containers.'); },
    });

    afterNextRender(() => {
      this.map = L.map(this.mapEl().nativeElement, { center: [18, 76], zoom: 5 });
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 18, attribution: '&copy; OpenStreetMap contributors',
      }).addTo(this.map);
      this.routes.addTo(this.map);
      this.ready.set(true);
      this.demoTimer = setInterval(() => this.tickDemo(), DEMO_TICK_MS);
    });

    // ports + route lines (coordinates come from Fleet, or the built-in port list)
    effect(() => {
      const list = this.containers();
      this.fleet.ports();
      if (!this.ready()) return;
      this.routes.clearLayers();
      const all: L.LatLngTuple[] = [];
      for (const c of list) {
        const o = portLatLng(this.fleet.ports().find((p) => p.portId === c.originPortId));
        const d = portLatLng(this.fleet.ports().find((p) => p.portId === c.destPortId));
        if (o && d) {
          L.polyline([o, d], { color: '#1976d2', weight: 3, dashArray: '6 8' }).addTo(this.routes);
          L.circleMarker(o, { radius: 6 }).bindTooltip(this.fleet.portName(c.originPortId)).addTo(this.routes);
          L.circleMarker(d, { radius: 6 }).bindTooltip(this.fleet.portName(c.destPortId)).addTo(this.routes);
          all.push(o, d);
        }
      }
      if (all.length && !this.fitted) { this.map.fitBounds(all, { padding: [40, 40] }); this.fitted = true; }
    });

    // ships driven by real (or staff-started simulated) GPS pings
    effect(() => {
      const pos = this.positions();
      if (!this.ready()) return;
      for (const p of Object.values(pos)) {
        this.demoStep.delete(p.containerId);   // real data wins over the dummy ship
        this.placeShip(p.containerId, [Number(p.latitude), Number(p.longitude)]);
      }
    });

    // follow the selected ship
    effect(() => {
      const id = this.selectedId();
      const p = id == null ? null : this.positions()[id];
      if (p && this.ready()) this.map.panTo([Number(p.latitude), Number(p.longitude)]);
    });

    inject(DestroyRef).onDestroy(() => {
      clearInterval(this.demoTimer);
      this.tracking.disconnect();
      this.map?.remove();
    });
  }

  protected select(id: number): void { this.selectedId.set(id); }

  /** True while the dummy ship (no real GPS) is the one being shown for this container. */
  protected isDemo(c: TrackedContainer): boolean {
    return c.status === 'IN_TRANSIT' && !this.positions()[c.containerId] && !!this.routeFor(c);
  }

  /**
   * No physical ship is connected, so when a container is IN_TRANSIT and no GPS ping has arrived,
   * a dummy ship sails origin -> destination on the map and loops.
   */
  private tickDemo(): void {
    if (!this.ready()) return;
    for (const c of this.containers()) {
      if (c.status !== 'IN_TRANSIT' || this.positions()[c.containerId]) continue;
      const route = this.routeFor(c);
      if (!route) continue;
      const i = ((this.demoStep.get(c.containerId) ?? -1) + 1) % route.length;
      this.demoStep.set(c.containerId, i);
      this.placeShip(c.containerId, route[i], true);
    }
  }

  private routeFor(c: TrackedContainer): L.LatLngTuple[] | null {
    const o = portLatLng(this.fleet.ports().find((p) => p.portId === c.originPortId));
    const d = portLatLng(this.fleet.ports().find((p) => p.portId === c.destPortId));
    return o && d ? routePoints(o, d, DEMO_STEPS) : null;
  }

  private placeShip(containerId: number, ll: L.LatLngTuple, demo = false): void {
    const m = this.markers.get(containerId);
    if (m) { m.setLatLng(ll); return; }
    this.markers.set(containerId,
      L.marker(ll, {
        icon: L.divIcon({ className: '', html: '<div class="ship">🚢</div>', iconSize: [28, 28], iconAnchor: [14, 14] }),
      })
        .bindTooltip(`Container #${containerId}${demo ? ' (simulated ship)' : ''}`)
        .on('click', () => this.selectedId.set(containerId))
        .addTo(this.map));
  }
}