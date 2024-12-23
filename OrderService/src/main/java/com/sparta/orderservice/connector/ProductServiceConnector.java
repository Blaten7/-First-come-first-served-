package com.sparta.orderservice.connector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class ProductServiceConnector {

    private final WebClient webClient;

    public ProductServiceConnector(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8060").build();
    }

    public boolean isProductExist(String productName) {
        try {
            Boolean isValid = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/product/isExist")
                            .queryParam("productName", productName)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return !Boolean.TRUE.equals(isValid);

        } catch (WebClientResponseException e) {
            System.err.println("Error response: " + e.getStatusCode());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return false;
        }
    }

    public boolean existByProductNameAndOverQuantity(String productName, int orderQuantity) {
        try {
            Boolean isValid = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/product/isOverQuantity")
                            .queryParam("productName", productName)
                            .queryParam("orderQuantity", orderQuantity)
                            .build())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return !Boolean.TRUE.equals(isValid);

        } catch (WebClientResponseException e) {
            System.err.println("Error response: " + e.getStatusCode());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return false;
        }
    }

    public void orderProduct(String productName, Integer orderQuantity) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/order")
                        .queryParam("productName", productName)
                        .queryParam("orderQuantity", orderQuantity)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void cancelProduct(String productName, int cancelQuantity) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/product/cancel")
                        .queryParam("productName", productName)
                        .queryParam("orderQuantity", cancelQuantity)
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
