package com.shipping.booking_service.dto.Response;

import java.time.LocalDateTime;

public record TimelineEventResponse(String status, String note, LocalDateTime at) {}
