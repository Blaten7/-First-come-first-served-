package com.sparta.purchaseservice.connector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderConnectionTest {

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

    private OrderConnection orderConnection;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just("success"));

        orderConnection = new OrderConnection(webClientBuilder);
    }

    @Test
    @DisplayName("결제 시작 요청 테스트")
    void startPaymentTest() {
        // given
        String token = "test-token";

        // when
        orderConnection.startPayment(token);

        // then
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(any(Function.class));
        verify(requestBodySpec).retrieve();
    }

    @Test
    @DisplayName("결제 완료 요청 테스트")
    void completePaymentTest() {
        // given
        String token = "test-token";

        // when
        orderConnection.completePayment(token);

        // then
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(any(Function.class));
        verify(requestBodySpec).retrieve();
    }
}