package com.sparta.purchaseservice;

import com.sparta.purchaseservice.connector.OrderConnection;
import com.sparta.purchaseservice.connector.ProductConnection;
import com.sparta.purchaseservice.controller.PurchaseController;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
class PurchaseServiceApplicationTests {

    @Mock
    private ProductConnection productConnection;

    @Mock
    private OrderConnection orderConnection;

    @InjectMocks
    private PurchaseController purchaseController;

    private WebTestClient webTestClient;

    @BeforeEach
    void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .directory("./PurchaseService/src/main/resources") // .env 파일 경로
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(purchaseController).build();
    }

    @Test
    void testStartPaymentProcess_CustomerAbandonment() {
        // Arrange: Mock ProductConnection
        doNothing().when(productConnection).cancelProduct();

        // Act: Send POST request
        webTestClient.post()
                .uri("/live/product/start")
                .header("Authorization", "Bearer test-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertEquals("고객이 결제 시도 중 이탈했습니다.", body);
                });

        // Assert: Verify cancelProduct was called
        verify(productConnection, times(1)).cancelProduct();
        verifyNoInteractions(orderConnection);
    }

    @Test
    void testStartPaymentProcess_SuccessfulPayment() {
        // Arrange: Mock OrderConnection and Complete Payment
        doNothing().when(orderConnection).startPayment("Bearer test-token");
        when(purchaseController.completePayment("Bearer test-token")).thenReturn(Mono.just(ResponseEntity.ok().body("")));

        // Act: Send POST request
        webTestClient.post()
                .uri("/live/product/start")
                .header("Authorization", "Bearer test-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertEquals("결제 프로세스가 시작되었습니다.", body);
                });

        // Assert: Verify interactions
        verify(orderConnection, times(1)).startPayment("Bearer test-token");
        verify(purchaseController, times(1)).completePayment("Bearer test-token");
        verifyNoInteractions(productConnection);
    }
}
