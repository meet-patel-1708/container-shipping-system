export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
export type PaymentMethod = 'UPI' | 'CARD' | 'NET_BANKING';

export interface CreateOrderResponse {
  paymentId: number; bookingId: number; orderId: string;
  amount: number; currency: string; keyId: string;
}
export interface VerifyRequest {
  razorpayOrderId: string; razorpayPaymentId: string; razorpaySignature: string;
}

export const PAYMENT_FILTERS: { label: string; value: PaymentStatus | '' }[] = [
  { label: 'All', value: '' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Paid', value: 'SUCCESS' },
  { label: 'Failed', value: 'FAILED' },
  { label: 'Refunded', value: 'REFUNDED' },
];

export interface Payment {
  id: number;
  bookingId: number;
  userId: number;
  amount: number;
  currency: string;
  paymentStatus: PaymentStatus;
  paymentMethod: string;
  transactionReference: string | null;
  paidAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentQuery { page: number; size: number; status: PaymentStatus | '' }

export const PAYMENT_METHODS: {
  value: PaymentMethod;
  label: string;
  icon: string;
}[] = [
  { value: 'UPI', label: 'UPI', icon: 'account_balance' },
  { value: 'CARD', label: 'Credit/Debit Card', icon: 'credit_card' },
  { value: 'NET_BANKING', label: 'Net Banking', icon: 'account_balance' },
];

export interface FailRequest {
  razorpayOrderId: string;
  razorpayPaymentId?: string;
  reason: string;
}