package com.healthcare.aiservice.security.filter;


import com.healthcare.aiservice.security.properties.HeaderRequestIdProperties;
import com.healthcare.aiservice.exception.rest_exception.RequestIdInvalideException;
import com.healthcare.aiservice.security.service.interfacies.RequestIdService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static com.healthcare.aiservice.security.filter.SecurityPaths.*;
import static com.healthcare.aiservice.security.filter.security.constant.AttrNames.ATTR_REQUEST_ID;


@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "request-id-filter.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RequestIdFilter extends OncePerRequestFilter {

    public final HeaderRequestIdProperties props;

    private final RequestIdService requestIdService;

    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestId = request.getHeader(props.name());
            if (!StringUtils.hasText(requestId)) {
                throw new RequestIdInvalideException(
                        "Header " + props.name() + " is required"
                );
            }
            requestId = requestId.strip();

            if (!requestIdService.isRequestIdValid(requestId)) {
                throw new RequestIdInvalideException(
                        "Header " + props.name() + " must be a valid UUID"
                );
            }

            log.debug(
                    "RequestId {} successfully validated.",
                    requestId
            );

            request.setAttribute(ATTR_REQUEST_ID, requestId);

            filterChain.doFilter(request, response);

        } catch (RequestIdInvalideException ex) {

            log.debug(
                    "RequestId is invalid."
            );

            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityPaths.shouldSkipSecurity(request);
    }
}
