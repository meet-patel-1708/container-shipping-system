package com.shipping.booking_service.event.outgoing;

public record BookingStatusChangedEvent(
        Long bookingId,
        Long userId,
        String oldStatus,
        String newStatus,
        String reason) {}