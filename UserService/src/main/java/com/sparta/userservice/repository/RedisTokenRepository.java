package com.sparta.userservice.repository;

import com.sparta.userservice.util.EncryptionUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisTokenRepository {

    private final StringRedisTemplate redisTemplate;

    public RedisTokenRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(String token, String userEmail, long expirationTimeInMillis) {
        String redisKey = "token:" + userEmail + ":" + token;
        redisTemplate.opsForValue().set(redisKey, token, expirationTimeInMillis, TimeUnit.MILLISECONDS);
    }

    // JWT 토큰 삭제 (현재 기기 로그아웃)
    public boolean removeToken(String token) {
        token = token.replace("Bearer ", "");
        Set<String> keys = redisTemplate.keys("token:*:" + token);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys.iterator().next());
            return true;
        }
        return false;
    }

    // 사용자 이메일 기반 토큰 삭제 (모든 기기 로그아웃)
    public int removeAllTokensByEmail(String email) throws Exception {
        System.out.println("레디스 토큰 레포지토리 진입");
        Set<String> keys = redisTemplate.keys("token:" + EncryptionUtil.decrypt(email) + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            return keys.size();
        }
        return 0;
    }
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }

}
