package com.aeon.common.exception;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.Instant;
import java.util.Map;

/**
 * Uniform error body returned by every microservice.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String correlationId;
    private final Map<String, String> fieldErrors;

    public ApiError(Instant timestamp, int status, String error, String message, String path,
                    String correlationId, Map<String, String> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId;
        this.fieldErrors = fieldErrors;
    }

    public Instant timestamp() { return timestamp; }
    public int status() { return status; }
    public String error() { return error; }
    public String message() { return message; }
    public String path() { return path; }
    public String correlationId() { return correlationId; }
    public Map<String, String> fieldErrors() { return fieldErrors; }
}
