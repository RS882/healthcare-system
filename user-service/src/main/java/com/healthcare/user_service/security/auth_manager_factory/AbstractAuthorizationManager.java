package com.healthcare.user_service.security.auth_manager_factory;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

import static com.healthcare.user_service.security.auth_manager_factory.util.AuthManagerSupport.isInvalidAuthentication;


public abstract class AbstractAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authenticationSupplier,
            RequestAuthorizationContext context
    ) {
        Authentication auth = authenticationSupplier.get();

        if (isInvalidAuthentication(auth)) {
            return new AuthorizationDecision(false);
        }

        return doCheck(auth, context);
    }

    protected abstract AuthorizationDecision doCheck(
            Authentication authentication,
            RequestAuthorizationContext context
    );
}
