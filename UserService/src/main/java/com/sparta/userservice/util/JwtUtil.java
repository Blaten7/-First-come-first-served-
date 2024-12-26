package com.sparta.userservice.util;

import com.sparta.userservice.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간

    public String generateToken(Member user) {
        return Jwts.builder()
                .claim("userEmail", user.getUserEmail())
                .claim("userId", user.getUserId())
                .claim("userName", user.getUserName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateTempToken(String email) {
        return Jwts.builder()
                .claim("userEmail", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userEmail", String.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다", e);
        }
    }

    public Authentication getAuthentication(String token) {
        String email = extractEmail(token);
        Member principal = new Member(email, "", new ArrayList<>()); // 권한이 없을 경우 빈 리스트 사용
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    public Mono<Boolean> isTokenValid(String token) {
        return Mono.fromCallable(() -> {
            try {
                Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }


    public long extractExpiration(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().getTime();
    }

}
