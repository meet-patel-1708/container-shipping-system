package com.shipping.auth_service.Exception;

public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException(String phone) {
        super("An account with email '" + phone + "' already exists");
    }
}
