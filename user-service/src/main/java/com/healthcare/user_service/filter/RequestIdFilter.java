package com.healthcare.user_service.filter;


import com.healthcare.user_service.config.properties.HeaderRequestIdProperties;
import com.healthcare.user_service.exception_handler.exception.RequestIdException;
import com.healthcare.user_service.service.interfacies.RequestIdService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_REQUEST_ID;

@Component
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
                throw new RequestIdException(
                        HttpStatus.BAD_REQUEST,
                        "Header " + props.name() + " is required"
                );
            }
            requestId = requestId.trim();
            if (!requestIdService.isRequestIdValid(requestId)) {
                throw new RequestIdException(
                        HttpStatus.BAD_REQUEST,
                        "Header " + props.name() + " must be a valid UUID"
                );
            }
            request.setAttribute(ATTR_REQUEST_ID, requestId);
            filterChain.doFilter(request, response);
        } catch (RequestIdException ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
