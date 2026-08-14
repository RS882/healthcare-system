package com.healthcare.user_service.security.internal_request.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.exception_handler.exception.InternalRequestAuthenticationServiceException;
import com.healthcare.user_service.exception_handler.exception.InternalRequestGrantInvalidException;
import com.healthcare.user_service.exception_handler.exception.InternalRequestGrantNotFoundException;
import com.healthcare.user_service.security.internal_request.dto.InternalRequestGrant;

import com.healthcare.user_service.security.internal_request.interfaces.InternalRequestGrantConsumer;
import com.healthcare.user_service.security.internal_request.properties.InternalRequestConsumerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisInternalRequestGrantConsumer
        implements InternalRequestGrantConsumer {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final InternalRequestConsumerProperties props;

    @Override
    public InternalRequestGrant consume(
            UUID internalRequestId
    ) {
        if (internalRequestId == null) {
            throw new InternalRequestGrantInvalidException(
                    "Internal request id must not be null"
            );
        }

        String serializedGrant;

        try {
            serializedGrant =
                    redisTemplate.opsForValue()
                            .getAndDelete(
                                    toRedisKey(internalRequestId)
                            );

        } catch (DataAccessException exception) {
            throw new InternalRequestAuthenticationServiceException(
                    "Internal request authentication service is unavailable",
                    exception
            );
        }

        if (serializedGrant == null
                || serializedGrant.isBlank()) {

            throw new InternalRequestGrantNotFoundException();
        }

        return deserialize(serializedGrant);
    }

    private InternalRequestGrant deserialize(
            String serializedGrant
    ) {
        try {
            return objectMapper.readValue(
                    serializedGrant,
                    InternalRequestGrant.class
            );

        } catch (JsonProcessingException exception) {
            throw new InternalRequestGrantInvalidException(
                    "Internal request grant is invalid",
                    exception
            );
        }
    }

    private String toRedisKey(
            UUID internalRequestId
    ) {
        return props.keyPrefix()
                + internalRequestId;
    }
}
