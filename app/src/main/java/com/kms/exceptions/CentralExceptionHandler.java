package com.kms.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Hidden
@RestControllerAdvice
public class CentralExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    return errors;
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public Map<String, String> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex) {
    if (ex.getMessage().contains("app_user_username_key")) {
      return Map.of("message", "Username already exists. Please choose another one.");
    }
    if (ex.getMessage().contains("app_user_email_key")) {
      return Map.of("message", "Email already exists. Please use a different one.");
    }

    return Map.of("message", "A data integrity violation occurred.");
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoSuchElementException.class)
  public Map<String, String> handleNoSuchElementException(NoSuchElementException ex) {
    return Map.of("message", ex.getMessage());
  }

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(BadCredentialsException.class)
  public Map<String, String> handleBadCredentialsException(BadCredentialsException ex) {
    return Map.of("message", ex.getMessage());
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(IOException.class)
  public Map<String, String> handleIOException(IOException ex) {
    return Map.of("message", ex.getMessage());
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(IllegalStateException.class)
  public Map<String, String> handleIllegalStateException(IllegalStateException ex) {
    return Map.of("message", ex.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalArgumentException.class)
  public Map<String, String> handleIllegalArgumentException(IllegalArgumentException ex) {
    return Map.of("message", ex.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidFormatException.class)
  public Map<String, String> handleInvalidFormatException(InvalidFormatException ex) {
    return Map.of("message", ex.getMessage());
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public Map<String, String> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    return Map.of("message", "Error while parsing input JSON: " + ex.getMessage());
  }

  // Handle when user access non-existing API endpoint
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NoHandlerFoundException.class)
  public Map<String, String> handleNoHandlerFoundException(NoHandlerFoundException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("path", ex.getRequestURL());
    response.put("message", "The requested endpoint does not exist.");

    return response;
  }

  // Handle any other exceptions
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public Map<String, String> handleGeneralException(Exception ex) {
    String exceptionMessage =
        (ex.getMessage() != null && !ex.getMessage().isEmpty())
            ? ex.getMessage()
            : "An unexpected error occurred.";
    return Map.of("message", exceptionMessage);
  }
}
