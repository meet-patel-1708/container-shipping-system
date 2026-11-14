package com.shipping.fleet_service.DTO.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PortResponse(
        Long id, String portName, String portAddress, String portCode,
        LocalDateTime createdAt, LocalDateTime updatedAt,
        BigDecimal latitude, BigDecimal longitude
) {}
