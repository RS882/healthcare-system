package com.healthcare.user_service.security.internal_request.resolver;

import com.healthcare.user_service.security.internal_request.constant.InternalAuthority;
import com.healthcare.user_service.security.internal_request.constant.InternalService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Internal service authority resolver tests")
class InternalServiceAuthorityResolverTest {

    private final InternalServiceAuthorityResolver resolver =
            new InternalServiceAuthorityResolver();

    @Test
    void should_grant_user_lookup_authority_to_auth_service() {

        Collection<GrantedAuthority> authorities =
                resolver.resolve(InternalService.AUTH_SERVICE);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(
                        InternalAuthority.USER_LOOKUP.authority()
                );
    }

    @Test
    void should_grant_user_auth_info_authority_to_ai_service() {

        Collection<GrantedAuthority> authorities =
                resolver.resolve(InternalService.AI_SERVICE);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly(
                        InternalAuthority.USER_AUTH_INFO.authority()
                );
    }

    @Test
    void auth_service_should_not_have_user_auth_info_authority() {

        Collection<GrantedAuthority> authorities =
                resolver.resolve(InternalService.AUTH_SERVICE);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .doesNotContain(
                        InternalAuthority.USER_AUTH_INFO.authority()
                );
    }

    @Test
    void ai_service_should_not_have_user_lookup_authority() {

        Collection<GrantedAuthority> authorities =
                resolver.resolve(InternalService.AI_SERVICE);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .doesNotContain(
                        InternalAuthority.USER_LOOKUP.authority()
                );
    }
}