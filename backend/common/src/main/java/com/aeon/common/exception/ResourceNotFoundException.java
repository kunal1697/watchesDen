package com.aeon.common.exception;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String resource, Object id) {
        super(404, resource + " not found: " + id);
    }
}
