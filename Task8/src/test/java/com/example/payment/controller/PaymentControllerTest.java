package com.example.payment.controller;

import com.example.dto.ProductResponse;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.dto.PaymentStatus;
import com.example.payment.exception.InsufficientFundsException;
import com.example.payment.service.PaymentService;
import com.example.product.ProductType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@Import(PaymentExceptionHandler.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    void getUserProductsReturnsOk() throws Exception {
        List<ProductResponse> responses = List.of(
                new ProductResponse(1L, "111", BigDecimal.TEN, ProductType.ACCOUNT, 1L)
        );
        when(paymentService.getProductsForUser(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/v1/payments/users/1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void executePaymentReturnsResponse() throws Exception {
        PaymentResponse paymentResponse = new PaymentResponse(
                10L,
                new BigDecimal("50.00"),
                new BigDecimal("150.00"),
                PaymentStatus.SUCCESS,
                "Payment successfully executed"
        );
        when(paymentService.executePayment(any(PaymentRequest.class))).thenReturn(paymentResponse);

        PaymentRequest request = new PaymentRequest(1L, 10L, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/payments/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.remainingBalance").value(150.00));
    }

    @Test
    void executePaymentReturnsErrorWhenInsufficientFunds() throws Exception {
        when(paymentService.executePayment(any(PaymentRequest.class)))
                .thenThrow(new InsufficientFundsException("Insufficient funds"));

        PaymentRequest request = new PaymentRequest(1L, 10L, new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/payments/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("Insufficient funds")));
    }
}


