package com.shipping.booking_service.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 404s: the requested resource itself doesn't exist ---
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookingNotFound(BookingNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ContainerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleContainerNotFound(ContainerNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- 400s: the request is well-formed but violates a business rule ---
    @ExceptionHandler(ContainerNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleContainerNotAvailable(ContainerNotAvailableException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidReferenceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidReference(InvalidReferenceException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- 409: valid request, but current state doesn't allow this action ---
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- @Valid failures on @RequestBody DTOs (field-level messages from records) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
        );
        ErrorResponse response = ErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST.value(), "Bad Request", errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    // --- @PathVariable/@RequestParam constraint violations (not on a @RequestBody) ---
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- wrong type in a path variable, e.g. /api/containers/status/NOT_A_REAL_STATUS ---
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format(
                "Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName()
        );
        return build(HttpStatus.BAD_REQUEST, message);
    }

    // --- catch-all: anything unexpected becomes a 500, never a raw stack trace to the client ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse response = ErrorResponse.of(status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status).body(response);
    }
    @ExceptionHandler(BookingAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleBookingAccessDenied(BookingAccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of(
                "status", ex.getStatus().value(),
                "error", ex.getStatus().getReasonPhrase(),
                "code", ex.getCode(),
                "message", ex.getMessage()));
    }
}
