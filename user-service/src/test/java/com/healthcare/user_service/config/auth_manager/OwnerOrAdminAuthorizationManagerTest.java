package com.healthcare.user_service.config.auth_manager;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.healthcare.user_service.controller.API.ApiPaths.PATH_VARIABLE_ID;
import static com.healthcare.user_service.support.TestDataFactory.anotherUserId;
import static com.healthcare.user_service.support.TestDataFactory.randomUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OwnerOrAdminAuthorizationManagerTest {

    private OwnerOrAdminAuthorizationManager authorizationManager;

    @BeforeEach
    void setUp() {
        authorizationManager = new OwnerOrAdminAuthorizationManager();
    }

    @Test
    @DisplayName("authorize: should deny when authentication is null")
    void authorize_shouldDeny_whenAuthenticationIsNull() {
        // Arrange
        Supplier<Authentication> authenticationSupplier = () -> null;
        RequestAuthorizationContext context = contextWithUserId(randomUserId());

        // Act
        AuthorizationDecision decision = authorizationManager.authorize(authenticationSupplier, context);

        // Assert
        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    @DisplayName("authorize: should deny for anonymous user")
    void authorize_shouldDeny_whenAnonymousUser() {
        // Arrange
        Authentication authentication = anonymousAuthentication();
        RequestAuthorizationContext context = contextWithUserId(randomUserId());

        // Act
        AuthorizationDecision decision = authorizationManager.authorize(() -> authentication, context);

        // Assert
        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    @DisplayName("authorize: should allow when role is admin")
    void authorize_shouldAllow_whenAdminRole() {
        // Arrange
        Authentication authentication = authentication(randomUserId(), Role.ROLE_ADMIN);
        RequestAuthorizationContext context = contextWithUserId(randomUserId());

        // Act
        AuthorizationDecision decision = authorizationManager.authorize(() -> authentication, context);

        // Assert
        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    @DisplayName("authorize: should allow when authenticated user is owner")
    void authorize_shouldAllow_whenUserIsOwner() {
        // Arrange
        Long userId = randomUserId();
        Authentication authentication = authentication(userId, Role.ROLE_PATIENT);
        RequestAuthorizationContext context = contextWithUserId(userId);

        // Act
        AuthorizationDecision decision = authorizationManager.authorize(() -> authentication, context);

        // Assert
        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    @DisplayName("authorize: should deny when user is not owner and not admin")
    void authorize_shouldDeny_whenUserIsNotOwnerAndNotAdmin() {
        // Arrange
        Long principalUserId = randomUserId();
        Long pathUserId = anotherUserId(principalUserId);

        Authentication authentication = authentication(principalUserId, Role.ROLE_PATIENT);
        RequestAuthorizationContext context = contextWithUserId(pathUserId);

        // Act
        AuthorizationDecision decision = authorizationManager.authorize(() -> authentication, context);

        // Assert
        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    @DisplayName("authorize: should deny when path variable id is missing")
    void authorize_shouldDeny_whenPathVariableIsMissing() {
        // Arrange
        Authentication authentication = authentication(randomUserId(), Role.ROLE_PATIENT);
        RequestAuthorizationContext context = contextWithoutUserId();

        // Act
        AuthorizationDecision decision = authorizationManager.authorize(() -> authentication, context);

        // Assert
        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    @DisplayName("authorize: should deny when principal has unexpected type")
    void authorize_shouldDeny_whenPrincipalHasUnexpectedType() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "unexpected-principal",
                null,
                List.of(new SimpleGrantedAuthority(Role.ROLE_PATIENT.name()))
        );
        RequestAuthorizationContext context = contextWithUserId(randomUserId());

        // Act
        AuthorizationDecision decision = authorizationManager.authorize(() -> authentication, context);

        // Assert
        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    @DisplayName("check: should delegate to authorize")
    void check_shouldDelegateToAuthorize() {
        // Arrange
        Long userId = randomUserId();
        Authentication authentication = authentication(userId, Role.ROLE_PATIENT);
        RequestAuthorizationContext context = contextWithUserId(userId);

        // Act
        AuthorizationDecision decision = authorizationManager.check(() -> authentication, context);

        // Assert
        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    @DisplayName("verify: should not throw when access is granted")
    void verify_shouldNotThrow_whenAccessGranted() {
        // Arrange
        Long userId = randomUserId();
        Authentication authentication = authentication(userId, Role.ROLE_PATIENT);
        RequestAuthorizationContext context = contextWithUserId(userId);

        // Act + Assert
        assertDoesNotThrow(() -> authorizationManager.verify(() -> authentication, context));
    }

    @Test
    @DisplayName("verify: should throw when access is denied")
    void verify_shouldThrow_whenAccessDenied() {
        // Arrange
        Long principalUserId = randomUserId();
        Long pathUserId = anotherUserId(principalUserId);

        Authentication authentication = authentication(principalUserId, Role.ROLE_PATIENT);
        RequestAuthorizationContext context = contextWithUserId(pathUserId);

        // Act + Assert
        assertThrows(
                AuthorizationDeniedException.class,
                () -> authorizationManager.verify(() -> authentication, context)
        );
    }

    private Authentication authentication(Long userId, Role role) {
        UserAuthInfoDto principal = new UserAuthInfoDto(
                userId,
                Set.of(role)
        );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(role.name()))
        );
    }

    private Authentication anonymousAuthentication() {
        return new AnonymousAuthenticationToken(
                "anonymous-key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );
    }

    private RequestAuthorizationContext contextWithUserId(Long userId) {
        return new RequestAuthorizationContext(
                null,
                Map.of(PATH_VARIABLE_ID, userId.toString())
        );
    }

    private RequestAuthorizationContext contextWithoutUserId() {
        return new RequestAuthorizationContext(
                null,
                Map.of()
        );
    }
}