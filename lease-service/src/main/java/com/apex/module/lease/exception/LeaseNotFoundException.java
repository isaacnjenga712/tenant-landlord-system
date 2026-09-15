package com.apex.module.lease.exception;

public class LeaseNotFoundException extends RuntimeException {
    public LeaseNotFoundException(String message) {
        super(message);
    }
}
