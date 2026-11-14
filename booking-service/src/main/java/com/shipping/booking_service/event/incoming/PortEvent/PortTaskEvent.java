package com.shipping.booking_service.event.incoming.PortEvent;

import java.math.BigDecimal;

public record PortTaskEvent(Long bookingId, Long containerId, Long originPortId, String scheduleDate, BigDecimal weightKg) {}
