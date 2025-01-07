package com.sparta.userservice.repository;

import com.sparta.userservice.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisTokenRepositoryTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisTokenRepository redisTokenRepository;

    @BeforeEach
    void setUp() {
        redisTokenRepository = new RedisTokenRepository(redisTemplate);
    }

    @Test
    @DisplayName("토큰 저장 테스트")
    void saveTokenTest() {
        // Given
        String token = "test-token";
        String userEmail = "test@example.com";
        long expirationTime = 3600000L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        redisTokenRepository.saveToken(token, userEmail, expirationTime);

        // Then
        verify(valueOperations).set(
                eq("token:" + userEmail + ":" + token),
                eq(token),
                eq(expirationTime),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("토큰 삭제 테스트")
    void removeTokenTest() {
        // Given
        String token = "Bearer test-token";
        Set<String> keys = new HashSet<>();
        keys.add("token:test@example.com:test-token");
        when(redisTemplate.keys(anyString())).thenReturn(keys);

        // When
        boolean result = redisTokenRepository.removeToken(token);

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("토큰 삭제 실패 테스트 - 토큰이 존재하지 않는 경우")
    void removeToken_WhenTokenNotExists() {
        // Given
        String token = "Bearer test-token";
        when(redisTemplate.keys(anyString())).thenReturn(new HashSet<>());

        // When
        boolean result = redisTokenRepository.removeToken(token);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("토큰 삭제 실패 테스트 - keys가 null인 경우")
    void removeToken_WhenKeysNull() {
        // Given
        String token = "Bearer test-token";
        when(redisTemplate.keys(anyString())).thenReturn(null);

        // When
        boolean result = redisTokenRepository.removeToken(token);

        // Then
        assertThat(result).isFalse();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("이메일 기반 토큰 전체 삭제 성공 테스트")
    void removeAllTokensByEmail_Success() throws Exception {
        // Given
        String encryptedEmail = "encrypted_email";
        String decryptedEmail = "test@example.com";
        Set<String> keys = new HashSet<>();
        keys.add("token:test@example.com:token1");
        keys.add("token:test@example.com:token2");

        try (MockedStatic<EncryptionUtil> mockedStatic = mockStatic(EncryptionUtil.class)) {
            mockedStatic.when(() -> EncryptionUtil.decrypt(encryptedEmail))
                    .thenReturn(decryptedEmail);
            when(redisTemplate.keys("token:" + decryptedEmail + ":*")).thenReturn(keys);

            // When
            int result = redisTokenRepository.removeAllTokensByEmail(encryptedEmail);

            // Then
            assertThat(result).isEqualTo(2);
            verify(redisTemplate).delete(keys);
        }
    }

    @Test
    @DisplayName("이메일 기반 토큰 전체 삭제 실패 테스트 - 토큰 없음")
    void removeAllTokensByEmail_NoTokens() throws Exception {
        // Given
        String encryptedEmail = "encrypted_email";
        String decryptedEmail = "test@example.com";
        Set<String> emptyKeys = new HashSet<>();

        try (MockedStatic<EncryptionUtil> mockedStatic = mockStatic(EncryptionUtil.class)) {
            mockedStatic.when(() -> EncryptionUtil.decrypt(encryptedEmail))
                    .thenReturn(decryptedEmail);
            when(redisTemplate.keys("token:" + decryptedEmail + ":*")).thenReturn(emptyKeys);

            // When
            int result = redisTokenRepository.removeAllTokensByEmail(encryptedEmail);

            // Then
            assertThat(result).isEqualTo(0);
            verify(redisTemplate, never()).delete(anySet());
        }
    }
    
    @Test
    @DisplayName("블랙리스트 토큰 체크 테스트")
    void isBlacklistedTest() {
        // Given
        String token = "test-token";
        when(redisTemplate.hasKey(token)).thenReturn(true);

        // When
        boolean result = redisTokenRepository.isBlacklisted(token);

        // Then
        assertThat(result).isTrue();
        verify(redisTemplate).hasKey(token);
    }
}