package com.healthcare.aiservice.security.filter;


import com.healthcare.aiservice.security.config.configs_components.CustomAuthenticationEntryPoint;
import com.healthcare.aiservice.security.filter.security.SignedUserContext;
import com.healthcare.aiservice.security.filter.security.interfaces.UserContextVerifier;
import com.healthcare.aiservice.security.properties.UserContextProperties;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.healthcare.aiservice.security.filter.security.constant.AttrNames.ATTR_USER_CONTEXT;


@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "user-context-filter.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class UserContextFilter extends OncePerRequestFilter {

    private final UserContextVerifier verifier;
    private final UserContextProperties userContextProps;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userContextToken = request.getHeader(userContextProps.userContextHeader());

        if (!StringUtils.hasText(userContextToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims =
                    verifier.verifyAndGetClaims(
                            userContextToken.strip()
                    );

            log.debug(
                    "Signed user context successfully verified."
            );

            SignedUserContext userContext =
                    SignedUserContext.from(claims);

            request.setAttribute(
                    ATTR_USER_CONTEXT,
                    userContext
            );

            filterChain.doFilter(request, response);

        } catch (SecurityException ex) {

            SecurityContextHolder.clearContext();

            log.debug(
                    "Signed user context verification failed.",
                    ex
            );

            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(ex.getMessage())

            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityPaths.shouldSkipSecurity(request);
    }
}