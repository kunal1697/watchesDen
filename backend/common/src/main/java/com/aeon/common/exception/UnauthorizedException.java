package com.aeon.common.exception;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public static UnauthorizedException forbidden(String message) {
        return new UnauthorizedException(403, message);
    }

    private UnauthorizedException(int status, String message) {
        super(status, message);
    }
}
