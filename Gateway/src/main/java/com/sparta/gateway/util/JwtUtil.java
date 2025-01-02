package com.sparta.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.core.env.Environment;

import java.security.Key;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class JwtUtil {

    static Environment env;

//    private static final String SECRET_KEY = env.getProperty("jwt.secret");
    private static final String SECRET_KEY = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUU=";
    private static final Key KEY = new SecretKeySpec(
            Base64.getDecoder().decode(SECRET_KEY), "HmacSHA256"
    );

    public static Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            throw new IllegalArgumentException("Invalid JWT signature");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
    }

    public static boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
