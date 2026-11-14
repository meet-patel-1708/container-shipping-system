package com.shipping.booking_service.dto.Response;

import com.shipping.booking_service.entity.ContainerType;
import com.shipping.booking_service.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContainerResponse(
        Long containerId,
        ContainerType containerType,
        Integer containerSize,
        String containerCompanyName,
        Status status,
        BigDecimal capacityKg,
        BigDecimal allocatedKg,
        BigDecimal remainingKg,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
