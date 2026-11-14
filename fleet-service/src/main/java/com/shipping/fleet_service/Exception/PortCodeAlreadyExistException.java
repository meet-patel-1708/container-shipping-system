package com.shipping.fleet_service.Exception;

public class PortCodeAlreadyExistException extends RuntimeException {
    public PortCodeAlreadyExistException(String message) {
        super(message);
    }
}
