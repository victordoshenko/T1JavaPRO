package com.example.payment.dto;

import java.time.Instant;

public record PaymentErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message
) {
}


