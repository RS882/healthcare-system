package com.healthcare.user_service.kafka.constant;

public enum OffsetResetPolicy {
    EARLIEST("earliest"),
    LATEST("latest");

    private final String value;

    OffsetResetPolicy(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
