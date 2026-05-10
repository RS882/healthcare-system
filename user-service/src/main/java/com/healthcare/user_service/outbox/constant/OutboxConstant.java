package com.healthcare.user_service.outbox.constant;

public class OutboxConstant {

    private OutboxConstant() {
    }

    public static final int MAX_ATTEMPTS = 5;

    public static final long RETENTION_DAYS = 7;
}
