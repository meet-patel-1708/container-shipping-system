package com.shipping.booking_service.exception;


public class ContainerNotFoundException extends RuntimeException {
    public ContainerNotFoundException(Long id) {
        super("Container not found with id: " + id);
    }
}