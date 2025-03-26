package com.healthcare.auth_service.service.interfacies;

public interface TokenBlacklistService {
    void blacklist(String accessToken, long ttl);

    boolean isBlacklisted(String accessToken);
}
