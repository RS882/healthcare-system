package com.healthcare.api_gateway.utilite;

import java.util.Objects;

public final class AttrKey<T> {
    private final String name;

    private AttrKey(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public static <T> AttrKey<T> of(String name) {
        return new AttrKey<>(name);
    }

    public String name() {
        return name;
    }
}

