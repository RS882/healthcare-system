package com.healthcare.auth_service.service;

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

    public static final String REQUEST_ID_VALUE = "Request Id";
    public static final long REQUEST_ID_TTL = 30_000L;

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
                id.toString(),
                REQUEST_ID_VALUE,
                Duration.ofMillis(REQUEST_ID_TTL)
        );
        return Boolean.TRUE.equals(result);
    }
}
