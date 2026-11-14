package com.shipping.booking_service.dto.Response;

public record BookingSummaryResponse(long activeBookings, long containersInTransit, long pendingPayments) {}
