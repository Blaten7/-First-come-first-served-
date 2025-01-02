package com.sparta.userservice.util;

import com.sparta.userservice.entity.Member;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1시간

    public String generateToken(Member user) {
        return Jwts.builder()
                .setSubject(user.getUserEmail())
                .claim("userId", user.getUserId())
                .claim("userName", user.getUserName())
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
                    .getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다", e);
        }
    }

//    public Authentication getAuthentication(String token) {
//        String email = extractEmail(token);
//
//        // 기본 권한 추가 (ROLE_USER)
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
//
//        // Member 객체 생성
//        Member principal = new Member(email, "", new ArrayList<>());
//
//        // UsernamePasswordAuthenticationToken 반환
//        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
//    }
}
