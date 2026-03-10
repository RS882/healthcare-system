package com.healthcare.user_service.filter;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.filter.security.SignedUserContext;
import com.healthcare.user_service.model.dto.UserAuthInfoDto;
import com.healthcare.user_service.service.interfacies.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_REQUEST_ID;
import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_USER_CONTEXT;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "auth-filter.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AuthFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        SignedUserContext ctx = extractValidUserContext(request);
        if (ctx == null) {
            filterChain.doFilter(request, response);
            return;
        }

        UserAuthInfoDto authDto = resolveUserAuthInfo(ctx);
        if (authDto == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!isUserContextConsistent(ctx, authDto)) {
            filterChain.doFilter(request, response);
            return;
        }

        setAuthentication(request, authDto);

        filterChain.doFilter(request, response);
    }

    private SignedUserContext extractValidUserContext(HttpServletRequest request) {
        Object attrRid = request.getAttribute(ATTR_REQUEST_ID);
        if (!(attrRid instanceof String requestId) || !StringUtils.hasText(requestId)) {
            return null;
        }

        Object attrCtx = request.getAttribute(ATTR_USER_CONTEXT);
        if (!(attrCtx instanceof SignedUserContext ctx)) {
            return null;
        }

        String normalizedRequestId = requestId.trim();
        String contextRequestId = ctx.requestId() == null ? null : ctx.requestId().trim();

        if (!StringUtils.hasText(contextRequestId)) {
            return null;
        }

        if (!normalizedRequestId.equals(contextRequestId)) {
            return null;
        }

        return ctx;
    }

    private UserAuthInfoDto resolveUserAuthInfo(SignedUserContext ctx) {
        String userCtxId = ctx.userId() == null ? null : ctx.userId().trim();

        if (!StringUtils.hasText(userCtxId)) {
            return null;
        }

        long userId;
        try {
            userId = Long.parseLong(userCtxId);
        } catch (NumberFormatException e) {
            return null;
        }

        if (userId <= 0) {
            return null;
        }

        return userService.getUserAuthInfoDtoById(userId);
    }

    private boolean isUserContextConsistent(SignedUserContext ctx, UserAuthInfoDto authDto) {
        if (authDto.getId() == null) {
            return false;
        }

        String userCtxId = ctx.userId() == null ? null : ctx.userId().trim();
        if (!StringUtils.hasText(userCtxId)) {
            return false;
        }

        if (!userCtxId.equals(String.valueOf(authDto.getId()))) {
            return false;
        }

        Set<Role> actualRoles = authDto.getRoles();
        List<String> tokenRoles = ctx.roles();

        if (actualRoles == null || actualRoles.isEmpty() || tokenRoles == null || tokenRoles.isEmpty()) {
            return false;
        }

        Set<String> actualRoleNames = actualRoles.stream()
                .map(Role::name)
                .collect(Collectors.toSet());

        Set<String> tokenRoleNames = tokenRoles.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toSet());

        return actualRoleNames.equals(tokenRoleNames);
    }

    private void setAuthentication(HttpServletRequest request, UserAuthInfoDto authDto) {
        List<SimpleGrantedAuthority> authorities = authDto.getRoles().stream()
                .map(Role::name)
                .map(SimpleGrantedAuthority::new)
                .toList();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(authDto, null, authorities);

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}