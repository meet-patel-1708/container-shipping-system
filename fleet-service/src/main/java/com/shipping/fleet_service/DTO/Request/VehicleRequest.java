package com.shipping.fleet_service.DTO.Request;

import com.shipping.fleet_service.Entity.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRequest(

        @NotBlank(message = "vehicle name must be exist")
        String vehicleName,


@NotBlank(message = "vehicle number must be exist")
String vehicleNumber,

@NotNull(message = "Vehicle Capacity must be greater than 0")
 int vehicleCapacity,


@NotNull(message = "vehicle Type is must be exist")
        @Enumerated(EnumType.STRING)
VehicleType vehicleType
) {
}
