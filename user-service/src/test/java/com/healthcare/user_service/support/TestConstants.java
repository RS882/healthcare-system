package com.healthcare.user_service.support;

public final class TestConstants {

    private TestConstants() {}

    public static final String HEADER_REQUEST_ID = "X-Request-Id-Test";
    public static final String HEADER_USER_CONTEXT = "X-User-Context-Test";

    public static final String TEST_BASE_URI = "/test";
    public final static String SECURE_URI = "/secure";
    public final static String PUBLIC_URI = "/public";

    public static final String REDIS_REQUEST_ID_PREFIX = "testPrefix:";

    public static final String BEARER = "Bearer ";

}
