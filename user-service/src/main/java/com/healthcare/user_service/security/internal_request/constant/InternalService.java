package com.healthcare.user_service.security.internal_request.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.healthcare.user_service.exception_handler.exception.UnknownInternalServiceException;

import java.util.Arrays;

public enum InternalService {

    AUTH_SERVICE("auth-service"),
    AI_SERVICE("ai-service");

    private final String serviceName;

    InternalService(String serviceName) {
        this.serviceName = serviceName;
    }

    @JsonValue
    public String serviceName() {
        return serviceName;
    }

    @JsonCreator
    public static InternalService fromServiceName(String serviceName) {
        return Arrays.stream(values())
                .filter(service ->
                        service.serviceName.equals(serviceName))
                .findFirst()
                .orElseThrow(() -> new UnknownInternalServiceException(serviceName)
                );
    }
}