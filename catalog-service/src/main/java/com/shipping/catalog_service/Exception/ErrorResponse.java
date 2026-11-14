package com.shipping.catalog_service.Exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> validationErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse ofValidation(int status, String error, String message, String path, List<String> validationErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}