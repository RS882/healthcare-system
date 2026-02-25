package com.healthcare.api_gateway.filter.support;


import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static String requestId() {
        return UUID.randomUUID().toString();
    }

    public static String requestIdWithSpaces() {
        return "  " + requestId() + "  ";
    }

    public static String headerTooLong(int len) {
        return "a".repeat(len);
    }

    public static String contextDefaultValue() {
        return "n/a";
    }

    public static String roleUser() {
        return "ROLE_USER_TEST";
    }

    public static String roleAdmin() {
        return "ROLE_ADMIN_TEST";
    }

    public static Long randomUserId() {
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }

    public static String singedJws() {
        return UUID.randomUUID().toString();
    }
}
