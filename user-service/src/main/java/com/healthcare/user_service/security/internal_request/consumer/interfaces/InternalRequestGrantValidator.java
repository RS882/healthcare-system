package com.healthcare.user_service.security.internal_request.consumer.interfaces;

import com.healthcare.user_service.security.internal_request.dto.InternalRequestGrant;
import jakarta.servlet.http.HttpServletRequest;

public interface InternalRequestGrantValidator {

    void validate(
            InternalRequestGrant grant,
            HttpServletRequest request
    );
}
