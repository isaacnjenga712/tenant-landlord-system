package com.apex.module.lease.exception;

public class LeaseUpdateNotAllowedException extends RuntimeException {
    public LeaseUpdateNotAllowedException(String message) {
        super(message);
    }
}
