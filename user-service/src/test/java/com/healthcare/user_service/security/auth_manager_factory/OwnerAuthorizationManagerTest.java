package com.healthcare.user_service.security.auth_manager_factory;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.healthcare.user_service.controller.API.ApiPaths.PATH_VARIABLE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OwnerAuthorizationManagerTest {

    private final OwnerAuthorizationManager manager = new OwnerAuthorizationManager();

    @Test
    void check_shouldGrantAccess_whenUserIsOwner() {
        Authentication auth = authenticatedUser(10L, Set.of(Role.ROLE_PATIENT));
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "10"));

        AuthorizationDecision decision = manager.check(authSupplier(auth), context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void check_shouldDenyAccess_whenUserIsNotOwner() {
        Authentication auth = authenticatedUser(10L, Set.of(Role.ROLE_PATIENT));
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "11"));

        AuthorizationDecision decision = manager.check(authSupplier(auth), context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void check_shouldDenyAccess_whenAuthenticationIsAnonymous() {
        Authentication auth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "10"));

        AuthorizationDecision decision = manager.check(authSupplier(auth), context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void check_shouldDenyAccess_whenPathVariableIsMissing() {
        Authentication auth = authenticatedUser(10L, Set.of(Role.ROLE_PATIENT));
        RequestAuthorizationContext context = requestContext(Map.of());

        AuthorizationDecision decision = manager.check(authSupplier(auth), context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    private static Supplier<Authentication> authSupplier(Authentication auth) {
        return () -> auth;
    }

    private static Authentication authenticatedUser(Long userId, Set<Role> roles) {
        UserAuthInfoDto principal = new UserAuthInfoDto(userId, roles);

        return org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                roles.stream()
                        .map(Role::name)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }

    private static RequestAuthorizationContext requestContext(Map<String, String> variables) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        return new RequestAuthorizationContext(request, variables);
    }
}