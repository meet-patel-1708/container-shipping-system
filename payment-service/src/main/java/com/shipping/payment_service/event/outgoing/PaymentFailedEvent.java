package com.shipping.payment_service.event.outgoing;


public record PaymentFailedEvent(
        Long paymentId,
        Long bookingId,
        Long userId,
        String reason
) {}