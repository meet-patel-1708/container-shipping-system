package com.shipping.fleet_service.Exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({
          PortNotFoundException.class,
          ShipNotFoundException.class,
          VehicleNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFound(
          RuntimeException ex,
          HttpServletRequest request
  ) {
    return build(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request
    );
  }

  @ExceptionHandler({
          PortCodeAlreadyExistException.class,
          ShipCodeAlreadyExistsException.class,
          VehicleNumberAlreadyExist.class
  })
  public ResponseEntity<ErrorResponse> handleDuplicate(
          RuntimeException ex,
          HttpServletRequest request
  ) {
    return build(
            HttpStatus.CONFLICT,
            ex.getMessage(),
            request
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
          MethodArgumentNotValidException ex,
          HttpServletRequest request
  ) {

    List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error ->
                    error.getField() + ": " +
                            error.getDefaultMessage()
            )
            .toList();

    ErrorResponse response = ErrorResponse.ofValidation(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation failed",
            request.getRequestURI(),
            errors
    );

    return ResponseEntity
            .badRequest()
            .body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(
          Exception ex,
          HttpServletRequest request
  ) {

    return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred",
            request
    );
  }

  private ResponseEntity<ErrorResponse> build(
          HttpStatus status,
          String message,
          HttpServletRequest request
  ) {

    ErrorResponse response = ErrorResponse.of(
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI()
    );

    return ResponseEntity
            .status(status)
            .body(response);
  }
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleInUse(DataIntegrityViolationException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, "Record is still in use", req);
  }
}