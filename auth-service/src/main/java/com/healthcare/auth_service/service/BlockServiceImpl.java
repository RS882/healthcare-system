package com.healthcare.auth_service.service;

import com.healthcare.auth_service.service.interfacies.BlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockServiceImpl implements BlockService {

    @Value("${jwt.access-token-expiration-ms}")
    private long accessExpirationMs;

    @Value("${prefix.blocked")
    private String blockedPrefix;

    private final StringRedisTemplate redis;

    @Override
    public void block(Long userId) {
        redis.opsForValue().set(getKey(userId), "blocked", Duration.ofMillis(accessExpirationMs));
        log.warn("User {} was blocked for {} seconds", userId, accessExpirationMs/1000);
    }

    @Override
    public boolean isBlocked(Long userId) {
        return Boolean.TRUE.equals(redis.hasKey(getKey(userId)));
    }

    private String getKey(Long userId) {
        return blockedPrefix + userId;
    }
}
