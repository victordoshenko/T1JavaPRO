package com.example.payment.controller;

import com.example.dto.ProductResponse;
import com.example.payment.dto.PaymentRequest;
import com.example.payment.dto.PaymentResponse;
import com.example.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/users/{userId}/products")
    public List<ProductResponse> getUserProducts(@PathVariable @Min(1) Long userId) {
        return paymentService.getProductsForUser(userId);
    }

    @PostMapping("/execute")
    public PaymentResponse executePayment(@Valid @RequestBody PaymentRequest request) {
        return paymentService.executePayment(request);
    }
}


