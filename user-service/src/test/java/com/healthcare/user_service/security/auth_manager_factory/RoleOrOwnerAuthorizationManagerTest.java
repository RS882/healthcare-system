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

import static com.healthcare.user_service.controller.API.ApiPaths.PATH_VARIABLE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class RoleOrOwnerAuthorizationManagerTest {

    @Test
    void constructor_shouldThrowException_whenRolesAreNull() {
        assertThatThrownBy(() -> new RoleOrOwnerAuthorizationManager(null))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }

    @Test
    void constructor_shouldThrowException_whenRolesAreEmpty() {
        assertThatThrownBy(() -> new RoleOrOwnerAuthorizationManager(Set.of()))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }

    @Test
    void check_shouldGrantAccess_whenUserHasAllowedRole() {
        RoleOrOwnerAuthorizationManager manager =
                new RoleOrOwnerAuthorizationManager(Set.of(Role.ROLE_ADMIN));
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "999"));

        AuthorizationDecision decision = manager.check(authSupplier(auth), context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void check_shouldGrantAccess_whenUserIsOwner() {
        RoleOrOwnerAuthorizationManager manager =
                new RoleOrOwnerAuthorizationManager(Set.of(Role.ROLE_ADMIN));
        Authentication auth = authenticatedUser(15L, Set.of(Role.ROLE_PATIENT));
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "15"));

        AuthorizationDecision decision = manager.check(authSupplier(auth), context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void check_shouldDenyAccess_whenUserIsNeitherOwnerNorHasAllowedRole() {
        RoleOrOwnerAuthorizationManager manager =
                new RoleOrOwnerAuthorizationManager(Set.of(Role.ROLE_ADMIN));
        Authentication auth = authenticatedUser(15L, Set.of(Role.ROLE_PATIENT));
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "99"));

        AuthorizationDecision decision = manager.check(authSupplier(auth), context);

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void constructor_shouldCreateDefensiveCopy() {
        Set<Role> mutableRoles = new HashSet<>(Set.of(Role.ROLE_ADMIN));
        RoleOrOwnerAuthorizationManager manager = new RoleOrOwnerAuthorizationManager(mutableRoles);

        mutableRoles.clear();

        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "999"));

        AuthorizationDecision decision = manager.check(authSupplier(auth), context);

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

    private static RequestAuthorizationContext requestContext(Map<String, String> variables) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        return new RequestAuthorizationContext(request, variables);
    }
}