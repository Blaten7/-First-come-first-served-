package com.sparta.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    private LoggingFilter loggingFilter;
    private GatewayFilterChain filterChain;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter();
        filterChain = mock(GatewayFilterChain.class);
        exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test/path").build()
        );
    }

    @Test
    @DisplayName("요청 성공 로깅 테스트")
    void successfulRequestLoggingTest() {
        // given
        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        // when & then
        StepVerifier.create(loggingFilter.filter(exchange, filterChain))
                .verifyComplete();
    }

    @Test
    @DisplayName("요청 실패 로깅 테스트")
    void failedRequestLoggingTest() {
        // given
        RuntimeException exception = new RuntimeException("Test error");
        when(filterChain.filter(exchange)).thenReturn(Mono.error(exception));

        // when & then
        StepVerifier.create(loggingFilter.filter(exchange, filterChain))
                .expectError(RuntimeException.class)
                .verify();
    }
}