package com.shipping.auth_service.Exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid Email And Password");
    }
}
