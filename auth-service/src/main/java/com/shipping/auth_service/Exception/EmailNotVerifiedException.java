package com.shipping.auth_service.Exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super("Please verify your email before login.");
    }
}
