package com.shipping.booking_service.event.incoming.VehicleEvent;

public record VehicleCreatedEvent(Long vehicleId, String vehicleName, String vehicleNumber, String vehicleType) {}
