package com.healthcare.user_service.security.internal_request.filter;

import com.healthcare.user_service.config.configs_components.CustomAuthenticationEntryPoint;
import com.healthcare.user_service.exception_handler.exception.InternalRequestAuthenticationServiceException;
import com.healthcare.user_service.exception_handler.exception.handler.InternalRequestAuthenticationServiceFailureHandler;
import com.healthcare.user_service.security.internal_request.authentication.InternalServiceAuthenticationToken;
import com.healthcare.user_service.security.internal_request.authentication.InternalServicePrincipal;
import com.healthcare.user_service.security.internal_request.constant.InternalService;
import com.healthcare.user_service.security.internal_request.consumer.interfaces.InternalRequestGrantValidator;
import com.healthcare.user_service.security.internal_request.dto.InternalRequestGrant;
import com.healthcare.user_service.security.internal_request.interfaces.InternalRequestGrantConsumer;
import com.healthcare.user_service.security.internal_request.properties.InternalRequestConsumerProperties;
import com.healthcare.user_service.security.internal_request.resolver.InternalServiceAuthorityResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "internal-request-filter.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class InternalRequestAuthenticationFilter
        extends OncePerRequestFilter {

    private final InternalRequestGrantConsumer grantConsumer;
    private final InternalRequestGrantValidator grantValidator;
    private final InternalServiceAuthorityResolver authorityResolver;
    private final InternalRequestConsumerProperties props;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final InternalRequestAuthenticationServiceFailureHandler
            authenticationServiceFailureHandler;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            String headerValue = request.getHeader(props.headerName());

            if (!StringUtils.hasText(headerValue)) {
                throw new BadCredentialsException("Internal request id header is required");
            }

            UUID internalRequestId = parseInternalRequestId(headerValue);

            InternalRequestGrant grant = grantConsumer.consume(internalRequestId);

            grantValidator.validate(grant, request);

            InternalService service = grant.issuer();

            Collection<GrantedAuthority> authorities = authorityResolver.resolve(service);

            InternalServicePrincipal principal = buildPrincipal(service);

            InternalServiceAuthenticationToken authentication =
                    new InternalServiceAuthenticationToken(
                            principal,
                            authorities
                    );

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

            securityContext.setAuthentication(authentication);

            SecurityContextHolder.setContext(securityContext);

            filterChain.doFilter(request, response);

        } catch (BadCredentialsException exception) {

            handleAuthenticationFailure(
                    request,
                    response,
                    exception
            );
        } catch (InternalRequestAuthenticationServiceException exception) {

            SecurityContextHolder.clearContext();

            authenticationServiceFailureHandler.handle(
                    request,
                    response,
                    exception
            );
        }
    }

    private UUID parseInternalRequestId(String headerValue) {
        try {
            return UUID.fromString(headerValue.strip());

        } catch (IllegalArgumentException exception) {
            throw new BadCredentialsException("Internal request id is invalid", exception);
        }
    }

    private InternalServicePrincipal buildPrincipal(InternalService service) {
        return InternalServicePrincipal.builder()
                .service(service)
                .authenticatedAt(Instant.now())
                .build();
    }

    private void handleAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            BadCredentialsException exception
    ) throws IOException {

        SecurityContextHolder.clearContext();

        authenticationEntryPoint.commence(
                request,
                response,
                exception
        );
    }
}
