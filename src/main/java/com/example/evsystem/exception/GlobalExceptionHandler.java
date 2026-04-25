package com.example.evsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException exception) {
        HttpStatus status = exception.getStatus();
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getField)
                .distinct()
                .collect(Collectors.joining(", "));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String detail = message.isBlank() ? "Validation failed." : "Missing or invalid fields: " + message;
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(), detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(), exception.getMessage()));
    }
}
