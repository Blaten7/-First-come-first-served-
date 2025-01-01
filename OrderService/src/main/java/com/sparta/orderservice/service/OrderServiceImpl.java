package com.sparta.orderservice.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
public class OrderServiceImpl implements OrderService {
    @Value("${jwt.secret}")
    private Key secretKey;

    @Override
    public String extractEmail(String token) {
        try {

            return Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Key 객체를 사용
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userEmail", String.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다", e);
        }
    }
}