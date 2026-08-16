package com.rgoncalo.financialapp.client;

/**
 * Specifies expected runtime exceptions that don't need to break the client app, sent to a user interface as an error message
 *
 */
public class ClientRuntimeException extends RuntimeException {
    public ClientRuntimeException(String message) {
        super(message);
    }
}
