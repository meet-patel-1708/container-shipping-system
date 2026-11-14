package com.shipping.booking_service.event;

public record BookingStatusChangedEvent(
        Long bookingId,
        Long userId,
        String oldStatus,
        String newStatus,
        String note
) {}