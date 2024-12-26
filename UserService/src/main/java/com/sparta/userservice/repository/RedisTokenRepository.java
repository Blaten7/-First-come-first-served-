package com.sparta.userservice.repository;

import com.sparta.userservice.util.EncryptionUtil;
import org.reactivestreams.Publisher;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class RedisTokenRepository {

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisTokenRepository(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Void> saveToken(String token, String userEmail, long expirationTimeInMillis) {
        String redisKey = "token:" + userEmail + ":" + token;
        return redisTemplate.opsForValue()
                .set(redisKey, token, Duration.ofMillis(expirationTimeInMillis))
                .then();
    }

    public Mono<Boolean> isTokenValid(String token) {
        String pattern = "token:*:" + token;
        return redisTemplate.keys(pattern)
                .hasElements();
    }

    public Mono<Boolean> removeToken(String token) {
        String pattern = "token:*:" + token;
        return redisTemplate.keys(pattern)
                .flatMap(redisTemplate::delete)
                .hasElements();
    }

    public Mono<Integer> removeAllTokensByEmail(String email) {
        return Mono.defer(() -> {
            String pattern = null;
            try {
                pattern = "token:" + EncryptionUtil.decrypt(email) + ":*";
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return redisTemplate.keys(pattern)
                    .collectList()
                    .flatMap(keys -> redisTemplate.delete((Publisher<String>) keys)
                            .thenReturn(keys.size()));
        });
    }

    public Mono<Void> saveTempToken(String token, String userEmail) {
        String redisKey = "TEMP:" + userEmail + ":" + token;
        return redisTemplate.opsForValue()
                .set(redisKey, token, Duration.ofMinutes(3))
                .then();
    }

    public Mono<Boolean> isTempTokenValid(String token, String email) {
        String redisKey = "TEMP:" + email + ":" + token;
        return redisTemplate.hasKey(redisKey);
    }

    public Mono<Void> addToBlacklist(String token, long expirationTimeInMillis) {
        return redisTemplate.opsForValue()
                .set(token, "blacklisted", Duration.ofMillis(expirationTimeInMillis))
                .then();
    }

    public Mono<Boolean> isBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
