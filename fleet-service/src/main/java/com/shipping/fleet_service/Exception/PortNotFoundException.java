package com.shipping.fleet_service.Exception;

public class PortNotFoundException extends RuntimeException {
    public PortNotFoundException(String message) {
        super(message);
    }
    public PortNotFoundException(Long id) {
        super(String.valueOf(id));
    }
}
