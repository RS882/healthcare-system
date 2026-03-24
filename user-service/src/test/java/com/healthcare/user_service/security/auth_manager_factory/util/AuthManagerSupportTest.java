package com.healthcare.user_service.security.auth_manager_factory.util;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.exception.InvalidRolesConfigurationException;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.healthcare.user_service.controller.API.ApiPaths.PATH_VARIABLE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class AuthManagerSupportTest {

    @Test
    void hasAnyRole_shouldReturnTrue_whenAuthenticationHasAtLeastOneRequiredRole() {
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));

        boolean result = AuthManagerSupport.hasAnyRole(auth, Set.of(Role.ROLE_ADMIN, Role.ROLE_DOCTOR));

        assertThat(result).isTrue();
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenAuthenticationHasNoRequiredRole() {
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_PATIENT));

        boolean result = AuthManagerSupport.hasAnyRole(auth, Set.of(Role.ROLE_ADMIN, Role.ROLE_DOCTOR));

        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenRolesAreNull() {
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));

        boolean result = AuthManagerSupport.hasAnyRole(auth, null);

        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenRolesAreEmpty() {
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));

        boolean result = AuthManagerSupport.hasAnyRole(auth, Set.of());

        assertThat(result).isFalse();
    }

    @Test
    void hasAnyRole_shouldReturnFalse_whenAuthenticationIsNull() {
        boolean result = AuthManagerSupport.hasAnyRole(null, Set.of(Role.ROLE_ADMIN));

        assertThat(result).isFalse();
    }

    @Test
    void hasRole_shouldReturnTrue_whenAuthenticationHasRequiredRole() {
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));

        boolean result = AuthManagerSupport.hasRole(auth, Role.ROLE_ADMIN);

        assertThat(result).isTrue();
    }

    @Test
    void hasRole_shouldReturnFalse_whenAuthenticationDoesNotHaveRequiredRole() {
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_PATIENT));

        boolean result = AuthManagerSupport.hasRole(auth, Role.ROLE_ADMIN);

        assertThat(result).isFalse();
    }

    @Test
    void hasRole_shouldReturnFalse_whenAuthenticationIsNull() {
        boolean result = AuthManagerSupport.hasRole(null, Role.ROLE_ADMIN);

        assertThat(result).isFalse();
    }

    @Test
    void hasRole_shouldReturnFalse_whenRoleIsNull() {
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));

        boolean result = AuthManagerSupport.hasRole(auth, null);

        assertThat(result).isFalse();
    }

    @Test
    void isOwner_shouldReturnTrue_whenPathVariableMatchesPrincipalUserId() {
        Authentication auth = authenticatedUser(15L, Set.of(Role.ROLE_PATIENT));
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "15"));

        boolean result = AuthManagerSupport.isOwner(auth, context);

        assertThat(result).isTrue();
    }

    @Test
    void isOwner_shouldReturnFalse_whenPathVariableDoesNotMatchPrincipalUserId() {
        Authentication auth = authenticatedUser(15L, Set.of(Role.ROLE_PATIENT));
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "99"));

        boolean result = AuthManagerSupport.isOwner(auth, context);

        assertThat(result).isFalse();
    }

    @Test
    void isOwner_shouldReturnFalse_whenPathVariableIsMissing() {
        Authentication auth = authenticatedUser(15L, Set.of(Role.ROLE_PATIENT));
        RequestAuthorizationContext context = requestContext(Map.of());

        boolean result = AuthManagerSupport.isOwner(auth, context);

        assertThat(result).isFalse();
    }

    @Test
    void isOwner_shouldReturnFalse_whenPathVariableIsNotNumeric() {
        Authentication auth = authenticatedUser(15L, Set.of(Role.ROLE_PATIENT));
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "abc"));

        boolean result = AuthManagerSupport.isOwner(auth, context);

        assertThat(result).isFalse();
    }

    @Test
    void isOwner_shouldReturnFalse_whenPrincipalHasUnexpectedType() {
        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
                "unexpected-principal",
                null,
                List.of(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()))
        );
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "15"));

        boolean result = AuthManagerSupport.isOwner(auth, context);

        assertThat(result).isFalse();
    }

    @Test
    void isOwner_shouldReturnFalse_whenAuthenticationIsNull() {
        RequestAuthorizationContext context = requestContext(Map.of(PATH_VARIABLE_ID, "15"));

        boolean result = AuthManagerSupport.isOwner(null, context);

        assertThat(result).isFalse();
    }

    @Test
    void isOwner_shouldReturnFalse_whenContextIsNull() {
        Authentication auth = authenticatedUser(15L, Set.of(Role.ROLE_PATIENT));

        boolean result = AuthManagerSupport.isOwner(auth, null);

        assertThat(result).isFalse();
    }

    @Test
    void isInvalidAuthentication_shouldReturnTrue_whenAuthenticationIsNull() {
        assertThat(AuthManagerSupport.isInvalidAuthentication(null)).isTrue();
    }

    @Test
    void isInvalidAuthentication_shouldReturnTrue_whenAuthenticationIsAnonymous() {
        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );

        assertThat(AuthManagerSupport.isInvalidAuthentication(auth)).isTrue();
    }

    @Test
    void isInvalidAuthentication_shouldReturnTrue_whenAuthenticationIsNotAuthenticated() {
        Authentication auth = new UsernamePasswordAuthenticationToken("principal", "credentials");

        assertThat(AuthManagerSupport.isInvalidAuthentication(auth)).isTrue();
    }

    @Test
    void isInvalidAuthentication_shouldReturnFalse_whenAuthenticationIsValid() {
        Authentication auth = authenticatedUser(1L, Set.of(Role.ROLE_ADMIN));

        assertThat(AuthManagerSupport.isInvalidAuthentication(auth)).isFalse();
    }

    @Test
    void validateRoles_shouldThrowException_whenRolesAreNull() {
        assertThatThrownBy(() -> AuthManagerSupport.validateRoles(null))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }

    @Test
    void validateRoles_shouldThrowException_whenRolesAreEmpty() {
        assertThatThrownBy(() -> AuthManagerSupport.validateRoles(Set.of()))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }

    @Test
    void validateRoles_shouldNotThrowException_whenRolesAreValid() {
        AuthManagerSupport.validateRoles(Set.of(Role.ROLE_ADMIN));
    }

    private static Authentication authenticatedUser(Long userId, Set<Role> roles) {
        UserAuthInfoDto principal = new UserAuthInfoDto(userId, roles);

        return UsernamePasswordAuthenticationToken.authenticated(
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