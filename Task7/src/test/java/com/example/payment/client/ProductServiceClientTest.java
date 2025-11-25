package com.example.payment.client;

import com.example.dto.ProductResponse;
import com.example.payment.exception.ProductNotFoundException;
import com.example.payment.exception.ProductServiceIntegrationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

@RestClientTest(ProductServiceClient.class)
@TestPropertySource(properties = "payment.product-service.base-url=http://localhost:8089")
class ProductServiceClientTest {

    @Autowired
    private ProductServiceClient client;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void getProductsForUserReturnsList() {
        mockServer.expect(requestTo("http://localhost:8089/api/v1/users/1/products"))
                .andRespond(MockRestResponseCreators.withSuccess("""
                        [
                          {"id":1,"accountNumber":"111","balance":100.0,"productType":"ACCOUNT","userId":1},
                          {"id":2,"accountNumber":"222","balance":200.0,"productType":"ACCOUNT","userId":1}
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<ProductResponse> products = client.getProductsForUser(1L);

        assertThat(products).hasSize(2);
    }

    @Test
    void getProductByIdThrowsWhenNotFound() {
        mockServer.expect(requestTo("http://localhost:8089/api/v1/products/55"))
                .andRespond(MockRestResponseCreators.withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

        assertThrows(ProductNotFoundException.class, () -> client.getProductById(55L));
    }

    @Test
    void getProductByIdThrowsOnServerError() {
        mockServer.expect(requestTo("http://localhost:8089/api/v1/products/10"))
                .andRespond(MockRestResponseCreators.withServerError());

        assertThrows(ProductServiceIntegrationException.class, () -> client.getProductById(10L));
    }

    @TestConfiguration
    static class RestTemplateConfig {

        @Bean
        RestTemplate restTemplate(RestTemplateBuilder builder) {
            return builder.build();
        }
    }
}


