package com.healthcare.auth_service.service;

import com.healthcare.auth_service.service.interfacies.BlockService;
import com.healthcare.auth_service.service.interfacies.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    private final StringRedisTemplate redis;
    private final BlockService blockService;

    private static final String PREFIX = "refresh:";
    private static final int MAX_TOKENS = 5;

    @Override
    public void save(String token, Long userId) {
        enforceSessionLimit(userId);

        String setKey = getSetKey(userId);

        redis.opsForSet().add(setKey, token);
        redis.expire(setKey, Duration.ofSeconds(refreshExpiration));

        redis.opsForValue().set(getKey(token, userId), "valid", Duration.ofSeconds(refreshExpiration));
    }

    @Override
    public boolean isValid(String token, Long userId) {
        return Boolean.TRUE.equals(redis.hasKey(getKey(token, userId)));
    }

    @Override
    public void delete(String token, Long userId) {
        redis.opsForSet().remove(getSetKey(userId), token);
        redis.delete(getKey(token, userId));
    }

    @Override
    public void deleteAll(Long userId) {
        String setKey = getSetKey(userId);
        Set<String> tokens = redis.opsForSet().members(setKey);

        if (tokens != null) {
            for (String token : tokens) {
                redis.delete(getKey(token, userId));
            }
        }
        redis.delete(setKey);
    }

    private void enforceSessionLimit(Long userId){
        if (blockService.isBlocked(userId)) {
            throw new RuntimeException("Превышен лимит сессий. Попробуйте позже.");
        }

        Long count = redis.opsForSet().size(getSetKey(userId));
        if (count != null && count >= MAX_TOKENS) {
            deleteAll(userId);
            blockService.block(userId);
            throw new RuntimeException("Слишком много активных сессий. Вы были временно заблокированы.");
        }
    }

    private String getKey(String token, Long userId) {
        return PREFIX + userId + ":" + token;
    }

    private String getSetKey(Long userId) {
        return  PREFIX + userId;
    }
}
