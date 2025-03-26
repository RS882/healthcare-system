package com.healthcare.auth_service.service.interfacies;

public interface BlockService {

    void block(Long userId);

    boolean isBlocked(Long userId);
}
