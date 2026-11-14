package com.shipping.fleet_service.Event;

public record VehicleUpdatedEvent(Long vehicleId,String vehicleName,String vehicleNumber,String vehicleType) {
}
