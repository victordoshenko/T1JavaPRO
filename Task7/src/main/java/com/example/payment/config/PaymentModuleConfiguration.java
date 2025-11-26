package com.example.payment.config;

import com.example.payment.client.ProductServiceResponseErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class PaymentModuleConfiguration {

    @Bean
    public ProductServiceResponseErrorHandler productServiceResponseErrorHandler() {
        return new ProductServiceResponseErrorHandler();
    }

    @Bean
    public RestTemplate paymentRestTemplate(RestTemplateBuilder builder,
                                            ProductServiceResponseErrorHandler errorHandler,
                                            @Value("${payment.product-service.base-url}") String baseUrl) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .rootUri(baseUrl)
                .errorHandler(errorHandler)
                .build();
    }
}


