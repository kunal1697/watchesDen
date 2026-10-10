package com.aeon.common.exception;

/**
 * A request that is well-formed but violates a business rule (e.g. insufficient stock, duplicate email).
 */
public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(409, message);
    }

    public BusinessException(int status, String message) {
        super(status, message);
    }
}
