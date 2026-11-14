package com.shipping.fleet_service.Exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String message) {
        super(message);
    }
    public VehicleNotFoundException(Long vehicleId) {
        super(String.valueOf(vehicleId));
    }
}
