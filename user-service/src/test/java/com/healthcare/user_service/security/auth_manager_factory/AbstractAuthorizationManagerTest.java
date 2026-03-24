package com.healthcare.user_service.security.auth_manager_factory;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AbstractAuthorizationManagerTest {

    @Test
    void check_shouldReturnDeniedAndNotInvokeDoCheck_whenAuthenticationIsNull() {
        AtomicBoolean doCheckCalled = new AtomicBoolean(false);
        TestAuthorizationManager manager = new TestAuthorizationManager(doCheckCalled, true);

        AuthorizationDecision decision = manager.check(() -> null, emptyContext());

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
        assertThat(doCheckCalled.get()).isFalse();
    }

    @Test
    void check_shouldReturnDeniedAndNotInvokeDoCheck_whenAuthenticationIsAnonymous() {
        AtomicBoolean doCheckCalled = new AtomicBoolean(false);
        TestAuthorizationManager manager = new TestAuthorizationManager(doCheckCalled, true);

        Authentication auth = new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        );

        AuthorizationDecision decision = manager.check(authSupplier(auth), emptyContext());

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
        assertThat(doCheckCalled.get()).isFalse();
    }

    @Test
    void check_shouldReturnDeniedAndNotInvokeDoCheck_whenAuthenticationIsNotAuthenticated() {
        AtomicBoolean doCheckCalled = new AtomicBoolean(false);
        TestAuthorizationManager manager = new TestAuthorizationManager(doCheckCalled, true);

        Authentication auth = new UsernamePasswordAuthenticationToken("principal", "credentials");

        AuthorizationDecision decision = manager.check(authSupplier(auth), emptyContext());

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
        assertThat(doCheckCalled.get()).isFalse();
    }

    @Test
    void check_shouldDelegateToDoCheck_whenAuthenticationIsValid() {
        AtomicBoolean doCheckCalled = new AtomicBoolean(false);
        TestAuthorizationManager manager = new TestAuthorizationManager(doCheckCalled, true);

        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
                "principal",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        AuthorizationDecision decision = manager.check(authSupplier(auth), emptyContext());

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isTrue();
        assertThat(doCheckCalled.get()).isTrue();
    }

    @Test
    void check_shouldReturnDecisionFromDoCheck_whenAuthenticationIsValid() {
        AtomicBoolean doCheckCalled = new AtomicBoolean(false);
        TestAuthorizationManager manager = new TestAuthorizationManager(doCheckCalled, false);

        Authentication auth = UsernamePasswordAuthenticationToken.authenticated(
                "principal",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        AuthorizationDecision decision = manager.check(authSupplier(auth), emptyContext());

        assertThat(decision).isNotNull();
        assertThat(decision.isGranted()).isFalse();
        assertThat(doCheckCalled.get()).isTrue();
    }

    private static Supplier<Authentication> authSupplier(Authentication auth) {
        return () -> auth;
    }

    private static RequestAuthorizationContext emptyContext() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        return new RequestAuthorizationContext(request, Map.of());
    }

    private static class TestAuthorizationManager extends AbstractAuthorizationManager {

        private final AtomicBoolean doCheckCalled;
        private final boolean granted;

        private TestAuthorizationManager(AtomicBoolean doCheckCalled, boolean granted) {
            this.doCheckCalled = doCheckCalled;
            this.granted = granted;
        }

        @Override
        protected AuthorizationDecision doCheck(Authentication authentication,
                                                RequestAuthorizationContext context) {
            doCheckCalled.set(true);
            return new AuthorizationDecision(granted);
        }
    }
}