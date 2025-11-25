package com.example.payment.service;

import com.example.dto.ProductResponse;
import com.example.payment.client.ProductServiceClient;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentStatus;
import com.example.payment.exception.InsufficientFundsException;
import com.example.payment.exception.ProductOwnershipException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class PaymentService {

    private final ProductServiceClient productServiceClient;

    public PaymentService(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    public List<ProductResponse> getProductsForUser(Long userId) {
        return productServiceClient.getProductsForUser(userId);
    }

    public PaymentResponse executePayment(PaymentRequest request) {
        ProductResponse product = productServiceClient.getProductById(request.productId());

        if (!Objects.equals(product.userId(), request.userId())) {
            throw new ProductOwnershipException("Product %d does not belong to user %d"
                    .formatted(product.id(), request.userId()));
        }

        validateAmount(product.balance(), request.amount(), product.id());

        BigDecimal remaining = product.balance().subtract(request.amount());

        return new PaymentResponse(
                product.id(),
                request.amount(),
                remaining,
                PaymentStatus.SUCCESS,
                "Payment successfully executed"
        );
    }

    private void validateAmount(BigDecimal balance, BigDecimal amount, Long productId) {
        if (amount.signum() <= 0) {
            throw new InsufficientFundsException("Payment amount must be greater than zero");
        }
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Product %d has insufficient funds. Balance=%s, requested=%s"
                            .formatted(productId, balance, amount)
            );
        }
    }
}


