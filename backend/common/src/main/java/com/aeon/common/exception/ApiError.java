package com.aeon.common.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Uniform error body returned by every microservice.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        Map<String, String> fieldErrors) {
}
