package com.shipping.payment_service.exception;

public class PaymentAlreadyProcessException extends RuntimeException {
    public PaymentAlreadyProcessException(Long bookingId) {
        super("A Payment Already Exist for booking id:"+bookingId);
    }
}
