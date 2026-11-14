package com.shipping.payment_service.event.outgoing;

public record PaymentSuccededEvent (
        Long paymentId,
        Long bookingId,
        Long userId,
        String transactionReference
) {}