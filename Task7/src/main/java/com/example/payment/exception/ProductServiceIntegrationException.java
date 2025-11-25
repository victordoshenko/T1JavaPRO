package com.example.payment.exception;

import org.springframework.http.HttpStatus;

public class ProductServiceIntegrationException extends PaymentModuleException {

    public ProductServiceIntegrationException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }

    public ProductServiceIntegrationException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_GATEWAY, cause);
    }
}


