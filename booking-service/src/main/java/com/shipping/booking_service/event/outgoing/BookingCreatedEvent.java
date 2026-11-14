package com.shipping.booking_service.event.outgoing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingCreatedEvent(
        Long bookingId,
        Long userId,
        Long containerId,
        Long shipId,
        BigDecimal totalAmount,
        LocalDateTime shippingDate,
        LocalDateTime arrivalDate
) {}