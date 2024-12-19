package com.sparta.domain.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RedisTokenRepository {

    private final StringRedisTemplate redisTemplate;

    public RedisTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String token, String userEmail, long expirationTimeInMillis) {
        redisTemplate.opsForValue().set(token, userEmail, expirationTimeInMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isTokenValid(String token) {
        return redisTemplate.hasKey(token);
    }

    public void deleteToken(String token) {
        redisTemplate.delete(token);
    }

    public void deleteAllTokens(String userEmail) {
        redisTemplate.keys("*").stream()
                .filter(key -> redisTemplate.opsForValue().get(key).equals(userEmail))
                .forEach(redisTemplate::delete);
    }
}
