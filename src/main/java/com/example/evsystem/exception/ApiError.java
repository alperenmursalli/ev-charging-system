package com.example.evsystem.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ApiError {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> validationErrors;

    public ApiError(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, message, path, Map.of());
    }

    public static ApiError validation(int status, String error, String message, String path, Map<String, String> validationErrors) {
        return new ApiError(status, error, message, path, validationErrors);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
