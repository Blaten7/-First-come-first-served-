package com.sparta.purchaseservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RedisConfigTest {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("Redis 연결 팩토리 생성 테스트")
    void redisConnectionFactoryTest() {
        assertNotNull(redisConnectionFactory);
        assertTrue(redisConnectionFactory instanceof LettuceConnectionFactory);
    }

    @Test
    @DisplayName("Redis 템플릿 설정 테스트")
    void redisTemplateTest() {
        assertNotNull(redisTemplate);
        assertTrue(redisTemplate.getKeySerializer() instanceof StringRedisSerializer);
        assertTrue(redisTemplate.getValueSerializer() instanceof StringRedisSerializer);
    }

    @Test
    @DisplayName("Redis Repository 활성화 테스트")
    void redisRepositoryEnabledTest() {
        EnableRedisRepositories annotation = RedisConfig.class.getAnnotation(EnableRedisRepositories.class);
        assertNotNull(annotation);
    }
}