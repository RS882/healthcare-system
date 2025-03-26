package com.healthcare.auth_service.service;

import com.healthcare.auth_service.service.interfacies.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    @Value("${jwt.access-token-expiration}")
    private long accessExpiration;

    private final StringRedisTemplate redis;

    private static final String PREFIX = "blacklist:";

    @Override
    public void blacklist(String accessToken) {
        if (!accessToken.isEmpty()) {
            redis.opsForValue().set(PREFIX + accessToken, "blacklisted", Duration.ofSeconds(accessExpiration));
        }
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + accessToken));
    }
}
