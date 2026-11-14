package com.shipping.booking_service.dto.Response;

import java.math.BigDecimal;

public record ContainerLoadResponse(Long containerId, BigDecimal capacityKg, BigDecimal allocatedKg, BigDecimal remainingKg) {}
