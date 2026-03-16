package com.healthcare.user_service.support;


import com.healthcare.user_service.constant.Role;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static UUID requestId() {
        return UUID.randomUUID();
    }

    public static String userName() {
        return "Test user";
    }

    public static String userEmail() {
        return "testexample@gmail.com";
    }

    public static String userPassword() {
        return "136Jkn!kPu5%";
    }

    public static String stringWithSpaces(String str) {
        return "  " + str + "  ";
    }

    public static String headerTooLong(int len) {
        return "a".repeat(len);
    }

    public static String contextDefaultValue() {
        return "n/a";
    }

    public static Role roleUser() {
        return Role.ROLE_PATIENT;
    }

    public static Role roleAdmin() {
        return Role.ROLE_ADMIN;
    }

    public static Long randomUserId() {
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }

    public static Long anotherUserId(Long userId) {
        Long another = randomUserId();
        while (another.equals(userId)) {
            another = randomUserId();
        }
        return another;
    }

    public static String singedJws() {
        return UUID.randomUUID().toString();
    }
}
