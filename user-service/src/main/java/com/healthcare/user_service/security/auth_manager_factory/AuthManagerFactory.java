package com.healthcare.user_service.security.auth_manager_factory;

import com.healthcare.user_service.constant.Role;

import java.util.Set;

public interface AuthManagerFactory {

    AbstractAuthorizationManager roleBased(Set<Role> roles);

    AbstractAuthorizationManager ownerBased();

    AbstractAuthorizationManager roleOrOwnerBased(Set<Role> roles);
}
