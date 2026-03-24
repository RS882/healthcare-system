package com.healthcare.user_service.security.auth_manager_factory;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import static com.healthcare.user_service.security.auth_manager_factory.util.AuthManagerSupport.isOwner;

public class OwnerAuthorizationManager extends AbstractAuthorizationManager {

    @Override
    protected AuthorizationDecision doCheck(Authentication auth,
                                            RequestAuthorizationContext context) {

        return new AuthorizationDecision(isOwner(auth, context));
    }
}
