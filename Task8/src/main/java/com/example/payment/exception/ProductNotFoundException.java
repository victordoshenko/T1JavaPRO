package com.example.payment.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends PaymentModuleException {

    public ProductNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, HttpStatus.NOT_FOUND, cause);
    }
}


