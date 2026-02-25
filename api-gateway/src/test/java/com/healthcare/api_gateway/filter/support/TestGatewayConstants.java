package com.healthcare.api_gateway.filter.support;

public final class TestGatewayConstants {

    private TestGatewayConstants() {}

    public static final String HEADER_REQUEST_ID = "X-Request-Id-Test";
    public static final String HEADER_USER_CONTEXT = "X-User-Context-Test";

    public static final String TEST_URI = "/test";
    public final static String AUTH_VALIDATION_URI = "/secure";

    public static final String REDIS_REQUEST_ID_PREFIX = "testPrefix:";

    public static final String BEARER = "Bearer ";

}
