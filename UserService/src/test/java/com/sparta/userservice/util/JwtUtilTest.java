package com.sparta.userservice.util;

import com.sparta.userservice.entity.Member;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET_KEY = "VGhpc0lzVGVzdFNlY3JldEtleUZvckpXVFRva2VuMTIzNDU2Nzg5MA=="; // Base64로 인코딩된 32바이트 키

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", SECRET_KEY);
    }

    @Test
    @DisplayName("토큰 생성 성공 테스트")
    void generateTokenSuccess() {
        // Given
        Member user = new Member();
        user.setUserId(1L);
        user.setUserEmail("test@example.com");
        user.setUserName("testUser");

        // When
        String token = jwtUtil.generateToken(user);

        // Then
        assertThat(token).isNotNull();
        String email = jwtUtil.extractEmail(token);
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 성공 테스트")
    void extractEmailSuccess() {
        // Given
        Member user = new Member();
        user.setUserEmail("test@example.com");
        String token = jwtUtil.generateToken(user);

        // When
        String extractedEmail = jwtUtil.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("잘못된 토큰으로 이메일 추출 실패 테스트")
    void extractEmailFailureInvalidToken() {
        // Given
        String invalidToken = "invalidToken";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractEmail(invalidToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 토큰입니다");
    }

    @Test
    @DisplayName("만료된 토큰 테스트")
    void expiredTokenTest() {
        // Given
        Member user = new Member();
        user.setUserEmail("test@example.com");
        String token = Jwts.builder()
                .setSubject(user.getUserEmail())
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000000))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractEmail(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCauseInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("잘못된 서명 토큰 테스트")
    void invalidSignatureTest() {
        // Given
        Key wrongKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        Member user = new Member();
        user.setUserEmail("test@example.com");
        String token = Jwts.builder()
                .setSubject(user.getUserEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000000))
                .signWith(SignatureAlgorithm.HS256, wrongKey)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractEmail(token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasCauseInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("토큰 null 테스트")
    void nullTokenTest() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractEmail(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 토큰입니다");
    }
}