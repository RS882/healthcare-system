package com.healthcare.auth_service.service;

import com.healthcare.auth_service.service.interfacies.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BlockServiceImpl implements BlockService {

    @Value("${jwt.access-token-expiration}")
    private long accessExpiration;

    private final StringRedisTemplate redis;

    private static final String BLOCKED_PREFIX = "refresh-block:";

    @Override
    public void block(Long userId) {
        String blockKey = BLOCKED_PREFIX + userId;
        redis.opsForValue().set(blockKey, "blocked", Duration.ofSeconds(accessExpiration));
    }

    @Override
    public boolean isBlocked(Long userId) {
        String blockKey = BLOCKED_PREFIX + userId;
        return Boolean.TRUE.equals(redis.hasKey(blockKey));
    }
}
