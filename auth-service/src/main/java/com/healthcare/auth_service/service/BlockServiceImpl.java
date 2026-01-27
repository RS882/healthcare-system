package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.JwtProperties;
import com.healthcare.auth_service.config.properties.PrefixProperties;
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

    private final JwtProperties jwtProps;
    private final PrefixProperties prefixProps;

    private final StringRedisTemplate redis;

    private final String BLOCKED_REDIS_VALUE = "blocked";

    @Override
    public void block(Long userId) {
        long expirationMs = jwtProps.accessTokenExpiration().toMillis();
        redis.opsForValue().set(getKey(userId), BLOCKED_REDIS_VALUE, expirationMs);
        log.warn("User {} was blocked for {} seconds", userId, expirationMs/1000);
    }

    @Override
    public boolean isBlocked(Long userId) {
        return Boolean.TRUE.equals(redis.hasKey(getKey(userId)));
    }

    private String getKey(Long userId) {
        return prefixProps.blocked() + userId;
    }
}
