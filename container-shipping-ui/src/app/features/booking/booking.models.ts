
export type ContainerStatus = 'AVAILABLE' | 'IN_TRANSIT' | 'MAINTENANCE' | 'RETIRED';
export type ContainerType = 'REGULAR' | 'AIR_CONDITIONER_CONTAINER';

export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'AT_PORT' | 'SHIPPED' | 'IN_TRANSIT'
  | 'ARRIVED' | 'DELIVERED' | 'REJECTED' | 'CANCELLED';
export const STATUS_FLOW: BookingStatus[] =
  ['PENDING', 'CONFIRMED', 'AT_PORT', 'SHIPPED', 'IN_TRANSIT', 'ARRIVED', 'DELIVERED'];
export const STATUS_LABEL: Record<BookingStatus, string> = {
  PENDING: 'Booked', CONFIRMED: 'Paid', AT_PORT: 'At port', SHIPPED: 'Shipped', IN_TRANSIT: 'In transit',
  ARRIVED: 'Arrived', DELIVERED: 'Delivered', REJECTED: 'Rejected', CANCELLED: 'Cancelled',
};
export const STATUS_FILTERS: { label: string; value: BookingStatus | '' }[] = [
  { label: 'All', value: '' },
  ...(['AT_PORT','SHIPPED','IN_TRANSIT','ARRIVED','DELIVERED','REJECTED','CANCELLED'] as BookingStatus[])
    .map((s) => ({ label: STATUS_LABEL[s], value: s })),
];

export interface BookingItem {
  itemId: number; productId: number; quantity: number; unitPrice: number; subtotal: number;productName: string | null;
}
export interface Booking {
shippingDate: string|number|Date;
  bookingId: number; containerId: number; userId: number; shipId: number;
  originPortId: number; destPortId: number; status: BookingStatus;
  totalAmount: number; items: BookingItem[]; createdAt: string; updatedAt: string;
  totalWeightKg: number;scheduleAt: string; containerCapacityKg: number; containerRemainingKg: number;
}
export interface BookingRequest {
  containerId: number; shipId: number; originPortId: number; destPortId: number; 
  items: { productId: number; quantity: number }[];scheduleAt: string;
}
export interface Container {
  containerId: number; containerType: ContainerType; containerSize: number;
  containerCompanyName: string; status: ContainerStatus; createdAt: string; updatedAt: string;
  capacityKg: number; allocatedKg: number; remainingKg: number;
}
export interface BookingQuery { page: number; size: number; status: BookingStatus | '' }

export interface TimelineEvent { status: BookingStatus; note: string | null; at: string }
export interface BookingSummary { activeBookings: number; containersInTransit: number; pendingPayments: number }
export interface MovementResult { containerId: number; status: BookingStatus; bookingsUpdated: number }

export interface ContainerLoad { containerId: number; capacityKg: number; allocatedKg: number; remainingKg: number }
