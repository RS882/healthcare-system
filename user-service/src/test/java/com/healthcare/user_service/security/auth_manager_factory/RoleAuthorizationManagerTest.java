package com.healthcare.user_service.security.auth_manager_factory;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.exception.InvalidRolesConfigurationException;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class RoleAuthorizationManagerTest {

    @Test
    void constructor_shouldThrowException_whenRolesAreNull() {
        assertThatThrownBy(() -> new RoleAuthorizationManager(null))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }

    @Test
    void constructor_shouldThrowException_whenRolesAreEmpty() {
        assertThatThrownBy(() -> new RoleAuthorizationManager(Set.of()))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }

    @Test
    void check_shouldGrantAccess_whenUserHasRequiredRole() {
        RoleAuthorizationManager manager = new RoleAuthorizationManager(Set.of(Role.ROLE_ADMIN));
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));

        AuthorizationDecision decision = manager.check(authSupplier(auth), emptyContext());

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void check_shouldGrantAccess_whenUserHasAnyAllowedRole() {
        RoleAuthorizationManager manager =
                new RoleAuthorizationManager(Set.of(Role.ROLE_ADMIN, Role.ROLE_DOCTOR));
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_DOCTOR));

        AuthorizationDecision decision = manager.check(authSupplier(auth), emptyContext());

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void check_shouldDenyAccess_whenUserHasNoAllowedRole() {
        RoleAuthorizationManager manager = new RoleAuthorizationManager(Set.of(Role.ROLE_ADMIN));
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_PATIENT));

        AuthorizationDecision decision = manager.check(authSupplier(auth), emptyContext());

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void constructor_shouldCreateDefensiveCopy() {
        Set<Role> mutableRoles = new HashSet<>(Set.of(Role.ROLE_ADMIN));
        RoleAuthorizationManager manager = new RoleAuthorizationManager(mutableRoles);

        mutableRoles.clear();

        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));
        AuthorizationDecision decision = manager.check(authSupplier(auth), emptyContext());

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
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

    private static RequestAuthorizationContext emptyContext() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        return new RequestAuthorizationContext(request, Map.of());
    }
}