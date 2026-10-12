package com.aeon.common.exception;

public class BusinessException extends ApiException {

    public BusinessException(String message) {
        super(409, message);
    }

    public BusinessException(int status, String message) {
        super(status, message);
    }
}
