package com.sparta.purchaseservice.service;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.connector.ProductConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private ProductConnection productConnection;

    @Mock
    private OrderConnection orderConnection;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @Test
    @DisplayName("결제 프로세스 시작 - 성공")
    void startPaymentProcessSuccess() {
        // given
        String token = "test-token";
        when(productConnection.cancelProduct()).thenReturn(Mono.empty());

        // when
        Mono<ResponseEntity<String>> result = purchaseService.startPaymentProcess(token);

        // then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode().is2xxSuccessful() ||
                                response.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();
    }

    @Test
    @DisplayName("결제 완료 - 성공")
    void completePaymentSuccess() {
        // given
        String token = "test-token";
        when(productConnection.cancelProduct()).thenReturn(Mono.empty());
        doNothing().when(orderConnection).completePayment(anyString());

        // when
        Mono<ResponseEntity<String>> result = purchaseService.completePayment(token);

        // then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode().is2xxSuccessful() ||
                                response.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();
    }

    @Test
    @DisplayName("결제 중 이탈 - 상품 취소")
    void paymentAbandonedTest() {
        // given
        String token = "test-token";
        when(productConnection.cancelProduct()).thenReturn(Mono.empty());

        // when
        Mono<ResponseEntity<String>> result = purchaseService.startPaymentProcess(token);

        // then
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.getStatusCode() == HttpStatus.NO_CONTENT ||
                                response.getStatusCode().is2xxSuccessful())
                .verifyComplete();
    }
}