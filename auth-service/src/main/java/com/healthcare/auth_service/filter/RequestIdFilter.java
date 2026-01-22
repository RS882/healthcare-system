package com.healthcare.auth_service.filter;

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


@Component
@RequiredArgsConstructor
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER_NAME = "X-Request-Id";

    private final RequestIdService requestIdService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = request.getHeader(REQUEST_ID_HEADER_NAME);
        if (requestId == null || requestId.isBlank()) {
            throw new RequestIdAuthenticationException(
                    HttpStatus.BAD_REQUEST,
                    "Header X-Request-Id is required"
            );
        }

        if (!requestIdService.isRequestIdValid(requestId)) {
            throw new RequestIdAuthenticationException(
                    HttpStatus.BAD_REQUEST,
                    "Header X-Request-Id must be a valid UUID"
            );
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
