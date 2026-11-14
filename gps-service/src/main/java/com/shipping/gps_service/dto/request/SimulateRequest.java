package com.shipping.gps_service.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record SimulateRequest(
        @NotNull Long containerId,
        @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal fromLat,
        @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal fromLng,
        @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal toLat,
        @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal toLng,
        @Min(2) @Max(300) Integer steps,          // default 60
        @Min(500) @Max(10000) Integer intervalMs  // default 2000
) {}