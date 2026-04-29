package com.example.evsystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        HttpStatus status = exception.getStatus();
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> validationErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String detail = validationErrors.isEmpty() ? "Validation failed." : "Request validation failed.";
        return ResponseEntity.status(status)
                .body(ApiError.validation(status.value(), status.getReasonPhrase(), detail, request.getRequestURI(), validationErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> validationErrors = exception.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.validation(status.value(), status.getReasonPhrase(), "Request validation failed.", request.getRequestURI(), validationErrors));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleHandlerMethodValidation(HandlerMethodValidationException exception, HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result -> {
            MethodParameter methodParameter = result.getMethodParameter();
            String parameterName = methodParameter != null ? methodParameter.getParameterName() : null;
            if (parameterName == null || parameterName.isBlank()) {
                parameterName = methodParameter != null ? "arg" + methodParameter.getParameterIndex() : "request";
            }
            String resolvedParameterName = parameterName;

            result.getResolvableErrors().forEach(error ->
                    validationErrors.put(resolvedParameterName, Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value."))
            );
        });

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.validation(status.value(), status.getReasonPhrase(), "Request validation failed.", request.getRequestURI(), validationErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage(HttpMessageNotReadableException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), "Malformed JSON request or invalid enum value.", request.getRequestURI()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.of(
                        status.value(),
                        status.getReasonPhrase(),
                        "Required request parameter '%s' is missing.".formatted(exception.getParameterName()),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), "Request conflicts with existing related data.", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), "Unexpected server error.", request.getRequestURI()));
    }
}
