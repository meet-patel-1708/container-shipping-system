package com.shipping.fleet_service.DTO.Response;

import com.shipping.fleet_service.Entity.VehicleType;

import java.time.LocalDateTime;

public record VehicleResponse (
    Long id,
    String vehicleName,
    String vehicleNumber,
    int vehicleCapacity,
    VehicleType vehicleType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt)
    {}
