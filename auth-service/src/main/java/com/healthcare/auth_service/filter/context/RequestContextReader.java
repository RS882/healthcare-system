package com.healthcare.auth_service.filter.context;

import com.healthcare.auth_service.exception_handler.exception.RequestIdAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static com.healthcare.auth_service.filter.context.constant.RequestContextAttributes.ATTR_REQUEST_ID;

public final class RequestContextReader {

    private RequestContextReader() {
    }

    public static Optional<UUID> getRequestId(HttpServletRequest request) {

        if (request == null) {
            return Optional.empty();
        }

        Object attribute = request.getAttribute(ATTR_REQUEST_ID);

        if (attribute instanceof UUID requestId) {
            return Optional.of(requestId);
        }

        return Optional.empty();
    }

    public static UUID getRequiredRequestId(
            HttpServletRequest request
    ) {
        return getRequestId(request)
                .orElseThrow(
                        () -> new RequestIdAuthenticationException(
                                HttpStatus.BAD_REQUEST,
                                "Validated request id is missing from request context"
                        )
                );
    }
}