package com.shipping.fleet_service.Exception;

public class ShipNotFoundException extends RuntimeException {
    public ShipNotFoundException(String message) {
        super(message);
    }
    public ShipNotFoundException(Long id) {
        super(String.valueOf(id));
    }
}
