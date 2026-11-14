package com.shipping.booking_service.exception;


public class InvalidReferenceException extends RuntimeException {
    public InvalidReferenceException(String entity, Long id) {
        super(entity + " does not exist: " + id);
    }
}
