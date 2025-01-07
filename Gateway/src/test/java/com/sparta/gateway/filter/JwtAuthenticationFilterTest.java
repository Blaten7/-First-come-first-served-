package com.sparta.gateway.filter;

import com.sparta.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private GatewayFilterChain chain;
    private JwtAuthenticationFilter.Config config;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        chain = mock(GatewayFilterChain.class);
        config = new JwtAuthenticationFilter.Config();
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("로그인 경로 요청시 토큰 검증 없이 통과")
    void loginPathTest() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/user/login").build()
        );

        // when
        Mono<Void> result = filter.apply(config).filter(exchange, chain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        verify(chain).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("회원가입 경로 요청시 토큰 검증 없이 통과")
    void signupPathTest() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/user/signup").build()
        );

        // when
        Mono<Void> result = filter.apply(config).filter(exchange, chain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        verify(chain).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("K6 테스트 회원가입 경로 요청시 토큰 검증 없이 통과")
    void k6SignupPathTest() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/user/k6/test/signup").build()
        );

        // when
        Mono<Void> result = filter.apply(config).filter(exchange, chain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        verify(chain).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("K6 테스트 회원삭제 경로 요청시 토큰 검증 없이 통과")
    void k6DeleteUserPathTest() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/user/k6/test/deleteUser").build()
        );

        // when
        Mono<Void> result = filter.apply(config).filter(exchange, chain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        verify(chain).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("Authorization 헤더가 없을 경우 401 반환")
    void noAuthHeaderTest() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/some-endpoint").build()
        );

        // when
        Mono<Void> result = filter.apply(config).filter(exchange, chain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Bearer 토큰이 아닌 경우 401 반환")
    void invalidBearerTokenTest() {
        // given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/some-endpoint")
                        .header(HttpHeaders.AUTHORIZATION, "Invalid token")
                        .build()
        );

        // when
        Mono<Void> result = filter.apply(config).filter(exchange, chain);

        // then
        StepVerifier.create(result)
                .verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("유효한 토큰으로 요청시 정상 처리")
    void validTokenTest() {
        // given
        String validToken = "valid.jwt.token";
        Claims validClaims = mock(Claims.class);
        when(validClaims.getSubject()).thenReturn("test@example.com");
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/some-endpoint")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                        .build()
        );

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(validToken)).thenReturn(validClaims);
            jwtUtil.when(() -> JwtUtil.isTokenExpired(validClaims)).thenReturn(false);

            // when
            Mono<Void> result = filter.apply(config).filter(exchange, chain);

            // then
            StepVerifier.create(result)
                    .verifyComplete();
            verify(chain).filter(any());
            assertThat(exchange.getResponse().getStatusCode()).isNull();
        }
    }

    @Test
    @DisplayName("만료된 토큰으로 요청시 401 반환")
    void expiredTokenTest() {
        // given
        String expiredToken = "expired.jwt.token";
        Claims expiredClaims = mock(Claims.class);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/some-endpoint")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                        .build()
        );

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(expiredToken)).thenReturn(expiredClaims);
            jwtUtil.when(() -> JwtUtil.isTokenExpired(expiredClaims)).thenReturn(true);

            // when
            Mono<Void> result = filter.apply(config).filter(exchange, chain);

            // then
            StepVerifier.create(result)
                    .verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Test
    @DisplayName("토큰 검증 실패시 401 반환")
    void tokenValidationFailureTest() {
        // given
        String invalidToken = "invalid.jwt.token";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/some-endpoint")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .build()
        );

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(() -> JwtUtil.validateToken(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

            // when
            Mono<Void> result = filter.apply(config).filter(exchange, chain);

            // then
            StepVerifier.create(result)
                    .verifyComplete();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}