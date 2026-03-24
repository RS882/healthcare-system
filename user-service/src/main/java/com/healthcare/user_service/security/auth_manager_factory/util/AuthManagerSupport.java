package com.healthcare.user_service.security.auth_manager_factory.util;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.exception.InvalidRolesConfigurationException;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Set;

import static com.healthcare.user_service.controller.API.ApiPaths.PATH_VARIABLE_ID;

public final class AuthManagerSupport {

    private AuthManagerSupport() {
    }

    public static boolean hasAnyRole(Authentication auth, Set<Role> roles) {

        if (roles == null || roles.isEmpty() || auth == null) {
            return false;
        }
        return roles.stream()
                .anyMatch(role -> hasRole(auth, role));
    }

    public static boolean hasRole(Authentication auth, Role role) {

        if (auth == null || role == null) {
            return false;
        }
        String requiredRole = role.name();

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredRole::equals);
    }

    public static boolean isOwner(Authentication auth, RequestAuthorizationContext context) {
        if (auth == null || context == null) {
            return false;
        }

        String pathUserId = context.getVariables().get(PATH_VARIABLE_ID);
        if (pathUserId == null) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserAuthInfoDto userPrincipal)) {
            return false;
        }

        try {
            Long requestedUserId = Long.valueOf(pathUserId);
            return requestedUserId.equals(userPrincipal.userId());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInvalidAuthentication(Authentication auth) {
        return auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken;
    }

    public static void validateRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new InvalidRolesConfigurationException();
        }
    }
}
