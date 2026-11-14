package com.shipping.booking_service.exception;

public class BookingAccessDeniedException extends RuntimeException {
    public BookingAccessDeniedException() {
        super("You are not authorized to access this booking");
    }
}