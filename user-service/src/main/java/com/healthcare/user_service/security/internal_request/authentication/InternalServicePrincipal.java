package com.healthcare.user_service.security.internal_request.authentication;


import com.healthcare.user_service.security.internal_request.constant.InternalService;
import lombok.Builder;

import java.time.Instant;

@Builder
public record InternalServicePrincipal(

        InternalService service,

        Instant authenticatedAt
) {
}
