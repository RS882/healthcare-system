package com.healthcare.auth_service.filter;

import com.healthcare.auth_service.config.properties.HeaderRequestIdProperties;
import com.healthcare.auth_service.exception_handler.exception.RequestIdAuthenticationException;
import com.healthcare.auth_service.service.interfacies.RequestIdService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.healthcare.auth_service.filter.context.constant.RequestContextAttributes.ATTR_REQUEST_ID;

@Component
@RequiredArgsConstructor
public class RequestIdFilter extends OncePerRequestFilter {

    private final HeaderRequestIdProperties props;

    private final RequestIdService requestIdService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String rawRequestId =
                request.getHeader(props.name());

        if (rawRequestId == null || rawRequestId.isBlank()) {
            throw new RequestIdAuthenticationException(
                    HttpStatus.BAD_REQUEST,
                    "Header " + props.name() + " is required"
            );
        }

        String requestId =
                rawRequestId.strip();

        if (!requestIdService.isRequestIdValid(requestId)) {
            throw new RequestIdAuthenticationException(
                    HttpStatus.BAD_REQUEST,
                    "Header " + props.name() + " must be a valid UUID"
            );
        }

        UUID requestIdValue =
                UUID.fromString(requestId);

        request.setAttribute(
                ATTR_REQUEST_ID,
                requestIdValue
        );

        filterChain.doFilter(
                request,
                response
        );
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityPaths.shouldSkipSecurity(request);
    }
}
