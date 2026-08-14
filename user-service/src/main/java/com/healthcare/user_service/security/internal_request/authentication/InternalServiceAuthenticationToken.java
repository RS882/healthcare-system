package com.healthcare.user_service.security.internal_request.authentication;


import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class InternalServiceAuthenticationToken
        extends AbstractAuthenticationToken {

    private final InternalServicePrincipal principal;

    public InternalServiceAuthenticationToken(
            InternalServicePrincipal principal,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public InternalServicePrincipal getPrincipal() {
        return principal;
    }
}
