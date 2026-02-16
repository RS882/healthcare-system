package com.healthcare.api_gateway.filter.constant;

import java.util.List;

public class ProtectedPrefixes {
    private ProtectedPrefixes() {
    }

    public static final List<String> PROTECTED_PREFIXES = List.of(
            "X-User-",
            "X-Internal-",
            "X-Auth-"
    );
}
