package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.PrefixProperties;
import com.healthcare.auth_service.service.interfacies.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final PrefixProperties prefixProps;

    private final StringRedisTemplate redis;

    @Override
    public void blacklist(String accessToken, long ttl) {
        if (StringUtils.hasText(accessToken) && ttl > 0) {
            redis.opsForValue().set(
                    prefixProps.blacklist() + accessToken.strip(),
                    "blacklisted",
                    Duration.ofMillis(ttl));
        }
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        if (!StringUtils.hasText(accessToken)) {
            return false;
        }
        return Boolean.TRUE.equals(redis.hasKey(prefixProps.blacklist() + accessToken.strip()));
    }
}
