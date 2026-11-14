package com.shipping.booking_service.event.outgoing;

public record BookingCancelledEvent(
        Long bookingId,
        Long userId,
        Long containerId
) {}