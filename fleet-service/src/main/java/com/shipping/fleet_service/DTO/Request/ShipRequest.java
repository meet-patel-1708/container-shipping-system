package com.shipping.fleet_service.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ShipRequest(

        @NotBlank(message = "Ship Company Name is required")
        String shipCompanyName,

        @NotBlank(message = "Ship Code is required")
        @Pattern(
                regexp = "^SH[0-9]{5}$",
                message = "Ship Code must start with SH followed by 5 digits"
        )
        String shipCode,

        @NotNull(message = "Ship Capacity is required")
        Long shipCapacity,

        @NotBlank(message = "Port Code is required")
        @Pattern(
                regexp = "^PC[0-9]{5}$",
                message = "Port Code must start with PC followed by 5 digits"
        )
        String portCode
) {
}