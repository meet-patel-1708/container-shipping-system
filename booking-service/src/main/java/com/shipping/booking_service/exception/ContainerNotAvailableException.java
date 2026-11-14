package com.shipping.booking_service.exception;


public class ContainerNotAvailableException extends RuntimeException {
    public ContainerNotAvailableException(Long id) {
        super("Container is not available for booking: " + id);
    }
}