package com.example.payment.exception;

import org.springframework.http.HttpStatus;

public class ProductOwnershipException extends PaymentModuleException {

    public ProductOwnershipException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}


