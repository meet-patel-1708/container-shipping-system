package com.shipping.booking_service.dto.Request;

import com.shipping.booking_service.entity.ContainerType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ContainerRequest(
        @NotNull(message = "Container type is required")
        ContainerType containerType,

        @NotNull(message = "Container size is required")
        Integer containerSize,

        @NotBlank(message = "Container company name is required")
        String containerCompanyName,
        @DecimalMin("1") BigDecimal capacityKg
) {}