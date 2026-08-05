package com.healthcare.aiservice.security.filter;


import com.healthcare.aiservice.security.config.configs_components.CustomAuthenticationEntryPoint;
import com.healthcare.aiservice.security.constant.Role;
import com.healthcare.aiservice.security.dto.UserAuthInfoDto;
import com.healthcare.aiservice.security.filter.security.SignedUserContext;
import com.healthcare.aiservice.security.service.interfacies.UserAuthInfoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.healthcare.aiservice.security.filter.security.constant.AttrNames.ATTR_REQUEST_ID;
import static com.healthcare.aiservice.security.filter.security.constant.AttrNames.ATTR_USER_CONTEXT;


@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "auth-filter.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AuthFilter extends OncePerRequestFilter {

    private final UserAuthInfoService userService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            SignedUserContext userContext = extractValidUserContext(request);

            UserAuthInfoDto authInfo =
                    resolveUserAuthInfo(userContext);

            validateConsistency(userContext, authInfo);

            setAuthentication(request, authInfo);

            filterChain.doFilter(request, response);

        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();

            authenticationEntryPoint.commence(
                    request,
                    response,
                    exception
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityPaths.shouldSkipSecurity(request);
    }

    private SignedUserContext extractValidUserContext(HttpServletRequest request) {

        Object attrRid = request.getAttribute(ATTR_REQUEST_ID);

        if (!(attrRid instanceof String requestId) || !StringUtils.hasText(requestId)) {
            throw new BadCredentialsException(
                    "Request id is missing");
        }

        Object attrCtx = request.getAttribute(ATTR_USER_CONTEXT);
        if (!(attrCtx instanceof SignedUserContext ctx)) {
            throw new BadCredentialsException(
                    "Signed user context is missing");
        }

        String contextRequestId = ctx.requestId();

        if (!StringUtils.hasText(contextRequestId)) {
            throw new BadCredentialsException(
                    "Signed user context does not have request id"
            );
        }
        String normalizedContextRequestId = contextRequestId.strip();
        String normalizedRequestId = requestId.strip();

        if (!normalizedRequestId.equals(normalizedContextRequestId)) {
            throw new BadCredentialsException(
                    "Request id does not match signed user context"
            );
        }

        return ctx;
    }

    private UserAuthInfoDto resolveUserAuthInfo(SignedUserContext ctx) {

        String userCtxId = ctx.userId();

        if (!StringUtils.hasText(userCtxId)) {
            throw new BadCredentialsException(
                    "Signed user context does not have request ID");
        }

        String normalizedUserCtxId = userCtxId.strip();

        long userId;
        try {
            userId = Long.parseLong(normalizedUserCtxId);
        } catch (NumberFormatException e) {
            throw new BadCredentialsException(
                    "User ID in signed user context is not a number");
        }

        if (userId <= 0) {
            throw new BadCredentialsException(
                    "User ID in signed user context is less than zero");
        }

        log.debug(
                "Validating authentication for userId={}.",
                userId
        );

        return userService.getUserAuthInfoByUserId(userId);
    }


    private void validateConsistency(SignedUserContext ctx, UserAuthInfoDto authDto) {

        if (authDto == null) {
            throw new BadCredentialsException(
                    "Authenticated user could not be resolved");
        }
        Long userId = authDto.userId();

        if (userId == null) {
            throw new BadCredentialsException(
                    "User ID in auth info dto must not be null.");
        }

        String userCtxId = ctx.userId();
        if (!StringUtils.hasText(userCtxId)) {
            throw new BadCredentialsException(
                    "User ID in signed user context must not be blank.");
        }

        String normalizedUserCtxId = userCtxId.strip();

        if (!normalizedUserCtxId.equals(String.valueOf(userId))) {
            throw new BadCredentialsException(
                    "User id in Signed user context does not match user id in current user data");
        }

        Set<Role> actualRoles = authDto.roles();
        List<String> tokenRoles = ctx.roles();

        if (actualRoles == null || actualRoles.isEmpty()) {
            throw new BadCredentialsException(
                    "Current user does not have roles.");
        }

        if (tokenRoles == null || tokenRoles.isEmpty()) {
            throw new BadCredentialsException(
                    "Signed user context does not have roles.");
        }

        Set<String> actualRoleNames = actualRoles.stream()
                .map(Role::name)
                .collect(Collectors.toSet());

        Set<String> tokenRoleNames = tokenRoles.stream()
                .filter(StringUtils::hasText)
                .map(String::strip)
                .collect(Collectors.toSet());

        if (!actualRoleNames.equals(tokenRoleNames)) {

            log.debug(
                    "Role validation failed for userId={}. Token roles={}, actual roles={}.",
                    userId,
                    tokenRoleNames,
                    actualRoleNames
            );

            throw new BadCredentialsException(
                    "Signed user context roles does not match current user roles");
        }

        log.debug(
                "Role validation succeeded for userId={}.",
                userId
        );
    }

    private void setAuthentication(HttpServletRequest request, UserAuthInfoDto authDto) {

        List<SimpleGrantedAuthority> authorities = authDto.roles().stream()
                .map(Role::name)
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(authDto, null, authorities);

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}