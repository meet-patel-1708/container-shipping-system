package com.shipping.fleet_service.DTO.Request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PortRequest(
        @NotBlank(message = "Port Name is mandatory") String portName,
        @NotBlank(message = "Port Address is not blank") String portAddress,
        @Pattern(regexp = "^PC[0-9]{5}$", message = "Port Code is not matched")
        @NotBlank(message = "Port Code is not blank") String portCode,
        @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude
) {}
