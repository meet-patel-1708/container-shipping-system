import { computed, inject, Injectable, signal } from '@angular/core';
import { forkJoin, map } from 'rxjs';
import { FleetApi } from './fleet.api';
import { ListQuery, PortOption, ShipOption, VehicleOption } from './fleet.model';

const ALL: ListQuery = { page: 0, size: 200, query: '', sort: '' };

@Injectable({ providedIn: 'root' })
export class FleetLookup {
  private readonly api = inject(FleetApi);

  readonly ports = signal<PortOption[]>([]);
  readonly ships = signal<ShipOption[]>([]);
  readonly vehicles = signal<VehicleOption[]>([]);
  readonly error = signal<string | null>(null);

  private readonly portMap = computed(() => new Map(this.ports().map((p) => [p.portId, p])));
  private readonly shipMap = computed(() => new Map(this.ships().map((s) => [s.shipId, s])));
  private readonly vehicleMap = computed(() => new Map(this.vehicles().map((v) => [v.vehicleId, v])));

  private loaded = false;

  portName = (id: number) => this.portMap().get(id)?.portName ?? `Port #${id}`;
  shipName = (id: number) => this.shipMap().get(id)?.shipCompanyName ?? `Ship #${id}`;
  vehicleName = (id: number) => this.vehicleMap().get(id)?.vehicleName ?? `Vehicle #${id}`;

  /** Loads once; safe to call from every component. */
  load(): void {
    if (this.loaded) return;
    this.loaded = true;
    forkJoin({
      ports: this.api.listPorts(ALL),
      ships: this.api.listShips(ALL),
      vehicles: this.api.listVehicles(ALL),
    })
      .pipe(
        map((r) => ({
          ports: r.ports.content.map((p): PortOption => ({
  portId: p.id, portName: p.portName, portCode: p.portCode,
  latitude: p.latitude ?? null, longitude: p.longitude ?? null,
})),
          ships: r.ships.content.map((s): ShipOption => ({
            shipId: s.id, shipCompanyName: s.shipCompanyName, shipCode: s.shipCode, portId: s.port.id,
          })),
          vehicles: r.vehicles.content.map((v): VehicleOption => ({
            vehicleId: v.id, vehicleName: v.vehicleName, vehicleNumber: v.vehicleNumber,
          })),
        })),
      )
      .subscribe({
        next: (r) => {
          this.ports.set(r.ports);
          this.ships.set(r.ships);
          this.vehicles.set(r.vehicles);
          this.error.set(null);
        },
        error: () => {
          this.loaded = false;
          this.error.set('Could not load ports, ships or vehicles from fleet-service.');
        },
      });
  }

  /** Call after creating, editing or deleting fleet data so dropdowns refresh. */
  reload(): void {
    this.loaded = false;
    this.load();
  }
  port = (id: number) => this.portMap().get(id);
}