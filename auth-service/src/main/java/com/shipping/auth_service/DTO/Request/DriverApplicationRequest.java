package com.shipping.auth_service.DTO.Request;

import com.shipping.auth_service.entity.DriverType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DriverApplicationRequest(
        @NotNull(message = "driverType is required")
        DriverType driverType,

        @Pattern(regexp = "^$|^[A-Za-z0-9 -]{5,30}$", message = "Invalid license number")
        String licenseNumber,

        @Min(value = 0, message = "experienceYears cannot be negative")
        @Max(value = 60, message = "experienceYears is too large")
        Integer experienceYears,

        @Pattern(regexp = "^$|^[A-Za-z0-9 -]{4,20}$", message = "Invalid vehicle number")
        String vehicleNumber
) {
}
