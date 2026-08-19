package com.rgoncalo.financialapp.infrastructure.persistence;

public class PersistenceException extends RuntimeException {
    public PersistenceException(String message) {
        super(message);
    }
    public PersistenceException(String message, Exception exception){
        super(message, exception);
    }
    public  PersistenceException(Exception e){
        super(e);
    }
}
