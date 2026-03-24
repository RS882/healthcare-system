package com.healthcare.user_service.security.auth_manager_factory;

import com.healthcare.user_service.constant.Role;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.healthcare.user_service.security.auth_manager_factory.util.AuthManagerSupport.validateRoles;

@Component
public class AuthManagerFactoryImpl implements AuthManagerFactory {

    private final AbstractAuthorizationManager ownerAuthorizationManager = new OwnerAuthorizationManager();

    @Override
    public AbstractAuthorizationManager roleBased(Set<Role> roles) {
        validateRoles(roles);
        return new RoleAuthorizationManager(roles);
    }

    @Override
    public AbstractAuthorizationManager ownerBased() {
        return ownerAuthorizationManager;
    }

    @Override
    public AbstractAuthorizationManager roleOrOwnerBased(Set<Role> roles) {
        validateRoles(roles);
        return new RoleOrOwnerAuthorizationManager(roles);
    }
}
