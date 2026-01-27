package com.healthcare.auth_service.service;

import com.healthcare.auth_service.config.properties.RequestIdProperties;
import com.healthcare.auth_service.exception_handler.exception.RequestIdSaveException;
import com.healthcare.auth_service.service.interfacies.RequestIdService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestIdServiceImpl implements RequestIdService {

    private final StringRedisTemplate redis;
    private final RequestIdProperties props;

    @Override
    public UUID getRequestId() {
        UUID id = UUID.randomUUID();
        if (!saveRequestId(id)) {
            throw new RequestIdSaveException();
        }
        return id;
    }

    @Override
    public boolean saveRequestId(UUID id) {
        Boolean result = redis.opsForValue().setIfAbsent(
                toRedisKey(id),
                props.value(),
                props.ttl()
        );
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean isRequestIdValid(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        try {
            UUID uuid = UUID.fromString(id);
            Boolean exists = redis.hasKey(toRedisKey(uuid));
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toRedisKey(UUID requestId) {
        return props.prefix() + requestId;
    }
}
