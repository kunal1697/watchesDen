package com.aeon.common.exception;

/**
 * Base class for all domain exceptions. Carries the HTTP status to return.
 */
public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
