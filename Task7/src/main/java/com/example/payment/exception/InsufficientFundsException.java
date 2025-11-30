package com.example.payment.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends PaymentModuleException {

    public InsufficientFundsException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}


