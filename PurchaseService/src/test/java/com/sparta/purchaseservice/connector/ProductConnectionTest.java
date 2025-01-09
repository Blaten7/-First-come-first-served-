package com.sparta.purchaseservice.connector;

import com.sparta.purchaseservice.dto.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductConnectionTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ProductConnection productConnection;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        productConnection = new ProductConnection(webClientBuilder);
    }

    @Test
    @DisplayName("선착순 상품 조회 테스트")
    void getFFProductTest() {
        // given
        Product testProduct = new Product(1L, "선착순", "테스트", 1000, 10);
        when(responseSpec.bodyToFlux(Product.class))
                .thenReturn(Flux.just(testProduct));

        // when
        Flux<Product> result = productConnection.getFFProduct();

        // then
        StepVerifier.create(result)
                .expectNextMatches(product ->
                        product.getProductName().equals("선착순") &&
                                product.getStockQuantity() == 10)
                .verifyComplete();
    }

    @Test
    @DisplayName("상품 취소 테스트")
    void cancelProductTest() {
        // given
        when(responseSpec.bodyToMono(Void.class))
                .thenReturn(Mono.empty());

        // when
        Mono<Void> result = productConnection.cancelProduct();

        // then
        StepVerifier.create(result)
                .verifyComplete();

        verify(requestBodyUriSpec).uri(any(Function.class));
        verify(requestBodySpec).retrieve();
    }
}