package com.healthcare.user_service.security.auth_manager_factory;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.exception.InvalidRolesConfigurationException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthManagerFactoryImplTest {

    private final AuthManagerFactoryImpl factory = new AuthManagerFactoryImpl();

    @Test
    void roleBased_shouldReturnRoleAuthorizationManager() {
        AbstractAuthorizationManager manager = factory.roleBased(Set.of(Role.ROLE_ADMIN));

        assertThat(manager).isInstanceOf(RoleAuthorizationManager.class);
    }

    @Test
    void roleBased_shouldThrowException_whenRolesAreNull() {
        assertThatThrownBy(() -> factory.roleBased(null))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }

    @Test
    void roleBased_shouldThrowException_whenRolesAreEmpty() {
        assertThatThrownBy(() -> factory.roleBased(Set.of()))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }

    @Test
    void ownerBased_shouldReturnOwnerAuthorizationManager() {
        AbstractAuthorizationManager manager = factory.ownerBased();

        assertThat(manager).isInstanceOf(OwnerAuthorizationManager.class);
    }

    @Test
    void ownerBased_shouldReturnSameInstanceEveryTime() {
        AbstractAuthorizationManager first = factory.ownerBased();
        AbstractAuthorizationManager second = factory.ownerBased();

        assertThat(first).isSameAs(second);
    }

    @Test
    void roleOrOwnerBased_shouldReturnRoleOrOwnerAuthorizationManager() {
        AbstractAuthorizationManager manager = factory.roleOrOwnerBased(Set.of(Role.ROLE_ADMIN));

        assertThat(manager).isInstanceOf(RoleOrOwnerAuthorizationManager.class);
    }

    @Test
    void roleOrOwnerBased_shouldThrowException_whenRolesAreNull() {
        assertThatThrownBy(() -> factory.roleOrOwnerBased(null))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }

    @Test
    void roleOrOwnerBased_shouldThrowException_whenRolesAreEmpty() {
        assertThatThrownBy(() -> factory.roleOrOwnerBased(Set.of()))
                .isInstanceOf(InvalidRolesConfigurationException.class);
    }
}