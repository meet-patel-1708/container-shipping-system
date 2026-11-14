export type ConnectionStatus = 'offline' | 'connecting' | 'connected' | 'reconnecting';

export interface GpsPosition {
  id: number; containerId: number; latitude: number; longitude: number;
  speedKmph: number | null; heading: number | null; recordedAt: string;
}
export interface TrackedContainer {
  containerId: number; status: 'SHIPPED' | 'IN_TRANSIT' | 'ARRIVED';
  originPortId: number; destPortId: number; shipId: number;
}