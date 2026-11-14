package com.shipping.fleet_service.Exception;

public class ShipCodeAlreadyExistsException extends RuntimeException {
    public ShipCodeAlreadyExistsException(String message) {
        super(message);
    }
}
