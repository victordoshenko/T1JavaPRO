package com.example.payment.dto;

import java.math.BigDecimal;

public record PaymentResponse(
        Long productId,
        BigDecimal amount,
        BigDecimal remainingBalance,
        PaymentStatus status,
        String message
) {
}


