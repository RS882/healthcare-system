package com.healthcare.auth_service.service.internal_request.interfaces;

import org.springframework.http.HttpMethod;

import java.util.UUID;

public interface InternalRequestGrantIssuer {

    UUID issue(
            String target,
            HttpMethod method,
            String path
    );
}
