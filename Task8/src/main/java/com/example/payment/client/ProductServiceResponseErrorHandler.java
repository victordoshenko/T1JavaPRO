package com.example.payment.client;

import com.example.payment.exception.ProductNotFoundException;
import com.example.payment.exception.ProductServiceIntegrationException;
import com.example.payment.exception.ProductServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ProductServiceResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        HttpStatusCode statusCode = response.getStatusCode();
        String body = extractBody(response);

        if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
            throw new ProductNotFoundException(buildMessage(statusCode, body));
        }

        if (statusCode.is5xxServerError()) {
            throw new ProductServiceUnavailableException(buildMessage(statusCode, body));
        }

        if (statusCode.is4xxClientError()) {
            throw new ProductServiceIntegrationException(buildMessage(statusCode, body));
        }

        super.handleError(response);
    }

    private String buildMessage(HttpStatusCode status, String body) {
        return "Product service responded with %s: %s"
                .formatted(status, body.isBlank() ? "no body" : body);
    }

    private String extractBody(ClientHttpResponse response) throws IOException {
        return StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
    }
}


