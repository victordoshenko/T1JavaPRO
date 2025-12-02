package com.example.limit.exception;

public class InsufficientLimitException extends RuntimeException {
    
    public InsufficientLimitException(String message) {
        super(message);
    }
}



