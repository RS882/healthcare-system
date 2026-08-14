package com.healthcare.user_service.security.internal_request.constant;

public enum InternalService {

    AUTH_SERVICE("auth-service"),
    AI_SERVICE("ai-service");

    private final String serviceName;

    InternalService(String serviceName) {
        this.serviceName = serviceName;
    }

    public String serviceName() {
        return serviceName;
    }

    public static InternalService fromServiceName(String serviceName) {
        for (InternalService service : values()) {
            if (service.serviceName.equals(serviceName)) {
                return service;
            }
        }

        throw new IllegalArgumentException(
                "Unknown internal service: " + serviceName
        );
    }
}
