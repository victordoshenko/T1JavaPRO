package com.example.payment.exception;

import org.springframework.http.HttpStatus;

public abstract class PaymentModuleException extends RuntimeException {

    private final HttpStatus status;

    protected PaymentModuleException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    protected PaymentModuleException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}


