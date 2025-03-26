package com.healthcare.auth_service.service;

import com.healthcare.auth_service.service.interfacies.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redis;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Override
    public void blacklist(String accessToken, long ttl) {
        if (StringUtils.hasText(accessToken) && ttl > 0) {
            redis.opsForValue().set(
                    BLACKLIST_PREFIX + accessToken,
                    "blacklisted",
                    Duration.ofMillis(ttl));
        }
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
