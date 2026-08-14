package com.healthcare.user_service.security.internal_request.dto;


import com.healthcare.user_service.security.internal_request.constant.InternalService;
import lombok.Builder;

import java.time.Instant;

@Builder
public record InternalRequestGrant(
        InternalService issuer,
        String target,
        String method,
        String path,
        Instant createdAt
) {
}
