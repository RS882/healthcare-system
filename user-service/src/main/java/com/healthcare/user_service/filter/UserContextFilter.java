package com.healthcare.user_service.filter;

import com.healthcare.user_service.config.properties.UserContextProperties;
import com.healthcare.user_service.filter.security.SignedUserContext;
import com.healthcare.user_service.filter.security.interfaces.UserContextVerifier;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_USER_CONTEXT;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "user-context-filter.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class UserContextFilter extends OncePerRequestFilter {

    private final UserContextVerifier verifier;
    private final UserContextProperties userContextProps;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userContextToken = request.getHeader(userContextProps.userContextHeader());

        if (StringUtils.hasText(userContextToken)) {

            Claims claims = verifier.verifyAndGetClaims(userContextToken);

            SignedUserContext userContext = SignedUserContext.from(claims);

            request.setAttribute(ATTR_USER_CONTEXT, userContext);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}




