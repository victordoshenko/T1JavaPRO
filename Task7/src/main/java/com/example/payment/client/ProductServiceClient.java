package com.example.payment.client;

import com.example.dto.ProductResponse;
import com.example.payment.exception.ProductServiceIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ProductServiceClient(RestTemplate restTemplate,
                                @Value("${payment.product-service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<ProductResponse> getProductsForUser(Long userId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/users/{userId}/products")
                .buildAndExpand(userId)
                .toUri();
        ProductResponse[] body = restTemplate.getForObject(uri, ProductResponse[].class);
        if (body == null || body.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(body);
    }

    public ProductResponse getProductById(Long productId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/products/{productId}")
                .buildAndExpand(productId)
                .toUri();
        ProductResponse product = restTemplate.getForObject(uri, ProductResponse.class);
        if (product == null) {
            throw new ProductServiceIntegrationException("Product service returned empty body for product %d"
                    .formatted(productId));
        }
        return product;
    }
}


