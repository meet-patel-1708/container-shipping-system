package com.shipping.payment_service.event.incoming;

public record BookingCancelledEvent(
        Long bookingId,
        Long userId,
        Long containerId
) {
}
