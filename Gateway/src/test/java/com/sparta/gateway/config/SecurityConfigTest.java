package com.sparta.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private WebTestClient webTestClient;

    @RestController
    static class TestController {
        @GetMapping("/test")
        public Mono<String> test() {
            return Mono.just("test");
        }

        @PostMapping("/test")
        public Mono<String> testPost() {
            return Mono.just("test");
        }
    }

    @Test
    @DisplayName("CORS 설정 테스트")
    void corsConfigurationTest() {
        webTestClient.options()
                .uri("/test")
                .header("Origin", "http://example.com")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "*")
                .expectHeader().valueEquals("Access-Control-Allow-Methods", "*")
                .expectHeader().valueEquals("Access-Control-Allow-Headers", "Authorization, Content-Type");
    }

    @Test
    @DisplayName("CSRF 비활성화 테스트")
    void csrfDisabledTest() {
        webTestClient.post()
                .uri("/test")
                .exchange()
                .expectStatus().is2xxSuccessful();  // 404 대신 2xx 상태 코드 확인
    }

    @Test
    @DisplayName("모든 엔드포인트 접근 허용 테스트")
    void permitAllEndpointsTest() {
        webTestClient.get()
                .uri("/test")
                .exchange()
                .expectStatus().is2xxSuccessful();  // 404 대신 2xx 상태 코드 확인

        webTestClient.post()
                .uri("/test")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}