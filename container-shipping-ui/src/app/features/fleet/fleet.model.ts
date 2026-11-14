export interface Port {
  id: number;
  portCode: string;
  portName: string;
  portAddress: string;
  latitude: number | null;
  longitude: number | null;
}
export interface Ship {
  id: number; shipCompanyName: string; shipCode: string; shipCapacity: number;
  port: Port; createdAt: string; updatedAt: string;
}
export type VehicleType = 'TRUCK' | 'TRAILER' | 'CRANE' | 'FROKLIFT';
export const VEHICLE_TYPES: { value: VehicleType; label: string }[] = [
  { value: 'TRUCK', label: 'Truck' },
  { value: 'TRAILER', label: 'Trailer' },
  { value: 'CRANE', label: 'Crane' },
  { value: 'FROKLIFT', label: 'Forklift' }, // backend spelling
];
export interface Vehicle {
  id: number; vehicleName: string; vehicleNumber: string; vehicleCapacity: number;
  vehicleType: VehicleType; createdAt: string; updatedAt: string;
}

export interface PortRequest {
  portCode: string;
  portName: string;
  portAddress: string;
  latitude: number | null;
  longitude: number | null;
}
export interface ShipRequest { shipCompanyName: string; shipCode: string; shipCapacity: number; portCode: string }
export interface VehicleRequest {
  vehicleName: string; vehicleNumber: string; vehicleCapacity: number; vehicleType: VehicleType;
}

export interface ListQuery { page: number; size: number; query: string; sort: string }

// Shapes the booking form's dropdowns use (mapped in FleetLookup)
export interface PortOption {
  portId: number; portName: string; portCode: string; latitude: number | null; longitude: number | null;
}
export interface ShipOption { shipId: number; shipCompanyName: string; shipCode: string; portId: number }
export interface VehicleOption { vehicleId: number; vehicleName: string; vehicleNumber: string }