package com.healthcare.auth_service.service.interfacies;

public interface RefreshTokenService {

    void save(String token, Long userId);

    boolean isValid(String token, Long userId);

    void delete(String token, Long userId);

    void deleteAll(Long userId);
}
