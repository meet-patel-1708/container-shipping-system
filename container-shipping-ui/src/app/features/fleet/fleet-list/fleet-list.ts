import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { PortsTab } from '../ports-tab/ports-tab';
import { ShipsTab } from '../ships-tab/ships-tab';
import { VehiclesTab } from '../vehicles-tab/vehicles-tab';

@Component({
  selector: 'app-fleet-list',
  imports: [MatTabsModule, PortsTab, ShipsTab, VehiclesTab],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <h2>Fleet</h2>
    <mat-tab-group animationDuration="150ms" dynamicHeight>
      <mat-tab label="Ports">
        <ng-template matTabContent>@defer { <app-ports-tab /> } @placeholder { <p>Loading…</p> }</ng-template>
      </mat-tab>
      <mat-tab label="Ships">
        <ng-template matTabContent>@defer { <app-ships-tab /> } @placeholder { <p>Loading…</p> }</ng-template>
      </mat-tab>
      <mat-tab label="Vehicles">
        <ng-template matTabContent>@defer { <app-vehicles-tab /> } @placeholder { <p>Loading…</p> }</ng-template>
      </mat-tab>
    </mat-tab-group>
  `,
  styles: `h2 { margin: 0 0 12px; }`,
})
export class FleetList {}