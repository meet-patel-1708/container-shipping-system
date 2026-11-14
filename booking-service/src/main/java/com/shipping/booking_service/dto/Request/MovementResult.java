package com.shipping.booking_service.dto.Request;

import com.shipping.booking_service.entity.BookingStatus;

public record MovementResult(Long containerId, BookingStatus status, int bookingsUpdated) {}
