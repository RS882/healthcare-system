package com.healthcare.user_service.security.auth_manager_factory;

import com.healthcare.user_service.constant.Role;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Set;

import static com.healthcare.user_service.security.auth_manager_factory.util.AuthManagerSupport.hasAnyRole;
import static com.healthcare.user_service.security.auth_manager_factory.util.AuthManagerSupport.validateRoles;

public class RoleAuthorizationManager extends AbstractAuthorizationManager {

    private final Set<Role> roles;

    public RoleAuthorizationManager(Set<Role> roles) {
        validateRoles(roles);
        this.roles = Set.copyOf(roles);
    }

    @Override
    protected AuthorizationDecision doCheck(Authentication auth,
                                            RequestAuthorizationContext context) {

        return new AuthorizationDecision(hasAnyRole(auth, roles));
    }
}