package com.rgoncalo.financialapp.application.security;

/**
 * Raised when supplied user credentials cannot be authenticated.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super("Invalid user name or password.");
    }
}
