package com.property.admin.exception;

public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String service, Throwable cause) {
        super("Upstream service unavailable: " + service, cause);
    }
}
