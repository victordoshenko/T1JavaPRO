package com.example.dto;

import com.example.product.Product;
import com.example.product.ProductType;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        ProductType productType,
        Long userId
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getAccountNumber(),
                product.getBalance(),
                product.getProductType(),
                product.getUser() != null ? product.getUser().getId() : null
        );
    }
}


