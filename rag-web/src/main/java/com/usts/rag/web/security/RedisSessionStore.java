package com.usts.rag.web.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usts.rag.common.security.AuthenticatedUser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * 基于 Redis 的登录态存储。
 */
@Component
public class RedisSessionStore {

    private static final String SESSION_PREFIX = "rag:session:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RedisSessionStore(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(String sessionId, AuthenticatedUser user, Duration ttl) {
        try {
            stringRedisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, objectMapper.writeValueAsString(user), ttl);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize authenticated user", exception);
        }
    }

    public Optional<AuthenticatedUser> get(String sessionId) {
        String payload = stringRedisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
        if (payload == null || payload.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(payload, AuthenticatedUser.class));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }
}
