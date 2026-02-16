package com.healthcare.api_gateway.filter.signing;


import java.time.Duration;
import java.util.List;

public interface UserContextSigner {
    String sign(String userId, List<String> roles, String requestId, Duration ttlSeconds);
}

