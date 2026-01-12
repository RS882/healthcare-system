package com.healthcare.auth_service.service.interfacies;

import java.util.UUID;

public interface RequestIdService {

    UUID getRequestId();

    boolean saveRequestId(UUID id);
}
