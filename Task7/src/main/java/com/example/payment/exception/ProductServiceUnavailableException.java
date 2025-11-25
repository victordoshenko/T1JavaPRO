package com.example.payment.exception;

import org.springframework.http.HttpStatus;

public class ProductServiceUnavailableException extends PaymentModuleException {

    public ProductServiceUnavailableException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, cause);
    }
}


