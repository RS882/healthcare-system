package com.healthcare.aiservice.security.internal_request.interfaces;

import org.springframework.http.HttpMethod;

import java.util.UUID;

public interface InternalRequestGrantIssuer {

    UUID issue(
            String targetService,
            HttpMethod method,
            String path
    );
}
