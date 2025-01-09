package com.sparta.orderservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private OrderServiceImpl orderService;
    private Key testSecretKey;

    @BeforeEach
    void setUp() {
        // 안전한 키 생성
        testSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        orderService = new OrderServiceImpl();
        ReflectionTestUtils.setField(orderService, "secretKey",
                Base64.getEncoder().encodeToString(testSecretKey.getEncoded()));
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 성공")
    void extractEmailSuccess() {
        // given
        String email = "test@test.com";
        String token = Jwts.builder()
                .claim("userEmail", email)
                .signWith(testSecretKey)
                .compact();

        // when
        String extractedEmail = orderService.extractEmail(token);

        // then
        assertEquals(email, extractedEmail);
    }


    @Test
    @DisplayName("잘못된 토큰으로 이메일 추출 실패")
    void extractEmailWithInvalidToken() {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                orderService.extractEmail(invalidToken)
        );
    }
}