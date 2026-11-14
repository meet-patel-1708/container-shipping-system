package com.shipping.auth_service.Exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("An account with user id '" + userId + "' not found");
    }
}
