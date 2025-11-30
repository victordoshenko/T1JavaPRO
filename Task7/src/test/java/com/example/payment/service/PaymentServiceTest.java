package com.example.payment.service;

import com.example.dto.ProductResponse;
import com.example.payment.client.ProductServiceClient;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentStatus;
import com.example.payment.exception.InsufficientFundsException;
import com.example.payment.exception.ProductOwnershipException;
import com.example.product.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private PaymentService paymentService;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productResponse = new ProductResponse(
                10L,
                "123456",
                new BigDecimal("100.00"),
                ProductType.ACCOUNT,
                1L
        );
    }

    @Test
    void getProductsForUserDelegatesToClient() {
        when(productServiceClient.getProductsForUser(1L)).thenReturn(List.of(productResponse));

        List<ProductResponse> result = paymentService.getProductsForUser(1L);

        assertEquals(1, result.size());
        verify(productServiceClient).getProductsForUser(1L);
    }

    @Test
    void executePaymentReturnsSuccessResponse() {
        PaymentRequest request = new PaymentRequest(1L, 10L, new BigDecimal("30.00"));
        when(productServiceClient.getProductById(10L)).thenReturn(productResponse);

        PaymentResponse response = paymentService.executePayment(request);

        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals(new BigDecimal("30.00"), response.amount());
        assertEquals(new BigDecimal("70.00"), response.remainingBalance());
    }

    @Test
    void executePaymentThrowsWhenProductBelongsToAnotherUser() {
        PaymentRequest request = new PaymentRequest(99L, 10L, new BigDecimal("10.00"));
        when(productServiceClient.getProductById(10L)).thenReturn(productResponse);

        assertThrows(ProductOwnershipException.class, () -> paymentService.executePayment(request));
    }

    @Test
    void executePaymentThrowsWhenInsufficientFunds() {
        PaymentRequest request = new PaymentRequest(1L, 10L, new BigDecimal("150.00"));
        when(productServiceClient.getProductById(10L)).thenReturn(productResponse);

        assertThrows(InsufficientFundsException.class, () -> paymentService.executePayment(request));
    }
}


