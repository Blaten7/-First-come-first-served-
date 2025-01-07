package com.sparta.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    private static final String TEST_SECRET_KEY = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUU=";
    private static final Key TEST_KEY = new SecretKeySpec(
            Base64.getDecoder().decode(TEST_SECRET_KEY),
            "HmacSHA256"
    );

    @Test
    @DisplayName("유효한 토큰 검증")
    void validateValidToken() {
        // given
        String validToken = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 1000000))
                .signWith(TEST_KEY)
                .compact();

        // when & then
        assertDoesNotThrow(() -> JwtUtil.validateToken(validToken));
    }

    @Test
    @DisplayName("잘못된 서명 검증")
    void validateInvalidSignature() {
        // given
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.wrongSignature";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> JwtUtil.validateToken(invalidToken));
    }

    @Test
    @DisplayName("만료된 토큰 검증")
    void validateExpiredToken() {
        // given
        Date expiredDate = new Date(System.currentTimeMillis() - 1000000);
        Claims claims = Jwts.claims().setExpiration(expiredDate);

        // when & then
        assertTrue(JwtUtil.isTokenExpired(claims));
    }

    @Test
    @DisplayName("유효하지 않은 토큰 형식 검증")
    void validateInvalidTokenFormat() {
        // given
        String invalidToken = "invalid.token.format";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> JwtUtil.validateToken(invalidToken));
    }
}