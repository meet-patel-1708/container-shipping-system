package com.shipping.fleet_service.Exception;

public class VehicleNumberAlreadyExist extends RuntimeException {
    public VehicleNumberAlreadyExist(String message) {
        super(message);
    }
}
