package com.example.payment.client;

import com.example.dto.ProductResponse;
import com.example.payment.exception.ProductNotFoundException;
import com.example.payment.exception.ProductServiceIntegrationException;
import com.example.payment.exception.ProductServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        try {
            ProductResponse[] body = restTemplate.getForObject(uri, ProductResponse[].class);
            if (body == null || body.length == 0) {
                return Collections.emptyList();
            }
            return Arrays.asList(body);
        } catch (ProductServiceIntegrationException ex) {
            throw new ProductServiceIntegrationException(
                    "Failed to fetch products for user %d: %s".formatted(userId, ex.getMessage()), ex);
        } catch (ProductServiceUnavailableException ex) {
            throw ex;
        } catch (HttpStatusCodeException ex) {
            throw new ProductServiceIntegrationException(errorMessage("user products", userId, ex), ex);
        } catch (RestClientException ex) {
            throw new ProductServiceUnavailableException("Product service is unavailable", ex);
        }
    }

    public ProductResponse getProductById(Long productId) {
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/v1/products/{productId}")
                .buildAndExpand(productId)
                .toUri();
        try {
            ProductResponse product = restTemplate.getForObject(uri, ProductResponse.class);
            if (product == null) {
                throw new ProductServiceIntegrationException("Product service returned empty body for product %d"
                        .formatted(productId));
            }
            return product;
        } catch (ProductNotFoundException ex) {
            throw new ProductNotFoundException("Product %d not found: %s".formatted(productId, ex.getMessage()), ex);
        } catch (ProductServiceIntegrationException ex) {
            throw new ProductServiceIntegrationException(
                    "Failed to fetch product %d: %s".formatted(productId, ex.getMessage()), ex);
        } catch (ProductServiceUnavailableException ex) {
            throw ex;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new ProductNotFoundException("Product %d not found".formatted(productId));
            }
            throw new ProductServiceIntegrationException(errorMessage("product", productId, ex), ex);
        } catch (RestClientException ex) {
            throw new ProductServiceUnavailableException("Product service is unavailable", ex);
        }
    }

    private String errorMessage(String subject, Long id, HttpStatusCodeException ex) {
        return "Product service responded with %s while retrieving %s '%d': %s"
                .formatted(ex.getStatusCode(), subject, id, extractBody(ex));
    }

    private String extractBody(HttpStatusCodeException ex) {
        return Optional.ofNullable(ex.getResponseBodyAsString()).orElse("no body");
    }
}


