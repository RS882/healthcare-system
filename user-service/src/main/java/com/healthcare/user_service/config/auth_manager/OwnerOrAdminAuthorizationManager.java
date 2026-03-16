package com.healthcare.user_service.config.auth_manager;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.healthcare.user_service.controller.API.ApiPaths.PATH_VARIABLE_ID;

@Component
public class OwnerOrAdminAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision authorize(
            Supplier<Authentication> authenticationSupplier,
            RequestAuthorizationContext context
    ) {
        Authentication authentication = authenticationSupplier.get();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return new AuthorizationDecision(false);
        }

        if (isAdmin(authentication) || isOwner(authentication, context)) {
            return new AuthorizationDecision(true);
        }
        return new AuthorizationDecision(false);
    }

    private boolean isAdmin(Authentication authentication) {

        String roleAdmin = Role.ROLE_ADMIN.name();

        return authentication.getAuthorities().stream()
                .anyMatch(a -> roleAdmin.equals(a.getAuthority()));
    }

    private boolean isOwner(Authentication authentication,
                            RequestAuthorizationContext context) {

        String pathUserId = context.getVariables().get(PATH_VARIABLE_ID);

        if (pathUserId == null) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserAuthInfoDto userPrincipal) {
            return pathUserId.equals(userPrincipal.userId().toString());
        }

        return false;
    }

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authenticationSupplier,
            RequestAuthorizationContext context
    ) {
        return authorize(authenticationSupplier, context);
    }

    @Override
    public void verify(
            Supplier<Authentication> authenticationSupplier,
            RequestAuthorizationContext context
    ) {
        AuthorizationManager.super.verify(authenticationSupplier, context);
    }
}