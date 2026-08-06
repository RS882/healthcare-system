package com.healthcare.auth_service.service.internal_request.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record InternalRequestGrant(
        String issuer,
        String target,
        String method,
        String path,
        Instant createdAt
) {
}
