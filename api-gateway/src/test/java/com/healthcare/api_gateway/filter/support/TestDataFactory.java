package com.healthcare.api_gateway.filter.support;


import java.util.UUID;

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
}
