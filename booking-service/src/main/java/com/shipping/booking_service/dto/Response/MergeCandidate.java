package com.shipping.booking_service.dto.Response;

import java.math.BigDecimal;

public record MergeCandidate(Long containerId, String containerType, int containerSize,
                             BigDecimal capacityKg, BigDecimal remainingKg, int openBookings) {}
