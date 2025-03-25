package com.healthcare.auth_service.service;

import com.healthcare.auth_service.service.interfacies.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    private final StringRedisTemplate redis;
    private static final String PREFIX = "refresh:";

    @Override
    public void save(String token, Long userId) {
        String key = getKey(token, userId);
        redis.opsForValue().set(key, "valid", Duration.ofSeconds(refreshExpiration));
    }

    @Override
    public boolean isValid(String token, Long userId) {
        String key = getKey(token, userId);
        return Boolean.TRUE.equals(redis.hasKey(key));
    }

    @Override
    public void delete(String token, Long userId) {
        String key = getKey(token, userId);
        redis.delete(key);
    }

    private String getKey(String token, Long userId) {
        return PREFIX + userId + ":" + token;
    }
}
