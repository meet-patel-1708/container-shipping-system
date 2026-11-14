package com.shipping.fleet_service.Event;

public record VehicleCreatedEvent(Long vehicleId,String vehicleName,String vehicleNumber,String vehicleType) {
}
