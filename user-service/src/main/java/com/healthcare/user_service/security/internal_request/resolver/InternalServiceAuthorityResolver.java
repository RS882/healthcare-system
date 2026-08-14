package com.healthcare.user_service.security.internal_request.resolver;


import com.healthcare.user_service.security.internal_request.constant.InternalAuthority;
import com.healthcare.user_service.security.internal_request.constant.InternalService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class InternalServiceAuthorityResolver {

    public Collection<? extends GrantedAuthority> resolve(
            InternalService service
    ) {
        return switch (service) {

            case AUTH_SERVICE -> List.of(
                    new SimpleGrantedAuthority(InternalAuthority.USER_LOOKUP.authority())
            );

            case AI_SERVICE -> List.of(
                    new SimpleGrantedAuthority(InternalAuthority.USER_AUTH_INFO.authority())
            );
        };
    }
}
