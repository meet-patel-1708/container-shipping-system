package com.shipping.fleet_service.DTO.Response;

import java.time.LocalDateTime;

public record ShipResponse(
        Long id,
        String shipCompanyName,
        String shipCode,
        Long shipCapacity,
        PortResponse port,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}