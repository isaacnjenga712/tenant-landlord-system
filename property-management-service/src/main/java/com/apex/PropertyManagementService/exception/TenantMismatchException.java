package com.apex.PropertyManagementService.exception;

public class TenantMismatchException extends RuntimeException {
	

    public TenantMismatchException(String message) {
        super(message);
    }

    public TenantMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
	


