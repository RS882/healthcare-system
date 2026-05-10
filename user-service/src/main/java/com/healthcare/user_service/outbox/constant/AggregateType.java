package com.healthcare.user_service.outbox.constant;

public enum AggregateType {

    AGGREGATE_TYPE_USER("USER"),

    UNKNOWN_AGGREGATE_TYPE("UNKNOWN");

    private final String type;

    AggregateType(String type) {
        this.type = type;
    }

    public String value() {
        return type;
    }
}
