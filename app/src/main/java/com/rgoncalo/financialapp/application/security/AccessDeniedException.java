package com.rgoncalo.financialapp.application.security;

/**
 * Raised when a user lacks the required access to a financial context.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
