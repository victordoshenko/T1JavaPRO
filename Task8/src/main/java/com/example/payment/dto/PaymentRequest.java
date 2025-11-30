package com.example.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "userId must be provided")
        Long userId,

        @NotNull(message = "productId must be provided")
        Long productId,

        @NotNull(message = "amount must be provided")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount
) {
}


