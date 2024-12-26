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

    // 특정 토큰 유효성 확인
//    public boolean isTokenValid(String token) {
//        Set<String> keys = redisTemplate.keys("token:*:" + token); // 패턴 검색
//        System.out.println("검색된 키: " + keys);
//        return keys != null && !keys.isEmpty();
//    }
    public boolean isTokenValid(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("token:*:" + token));
    }


    // JWT 토큰 삭제 (현재 기기 로그아웃)
    public boolean removeToken(String token) {
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

    public void saveTempToken(String token, String userEmail) {
        String redisKey = "TEMP:" + userEmail + ":" + token;
        redisTemplate.opsForValue().set(redisKey, token, 3 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

//    public boolean isTempTokenValid(String token, String email) {
//        Set<String> keys = redisTemplate.keys("TEMP:" + email + ":" + token); // 패턴 검색
//        System.out.println("검색된 키: " + keys);
//        return keys != null && !keys.isEmpty();
//    }
    public boolean isTempTokenValid(String token, String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("TEMP:" + email + ":" + token));
    }
}
