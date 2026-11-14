package com.shipping.gps_service.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LocationPingRequest(
        @NotNull(message = "Container id is required")
        Long containerId,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        BigDecimal latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        BigDecimal longitude,

        BigDecimal speedKmph,

        @DecimalMin(value = "0.0", message = "Heading must be >= 0")
        @DecimalMax(value = "360.0", message = "Heading must be <= 360")
        BigDecimal heading
) {
}
