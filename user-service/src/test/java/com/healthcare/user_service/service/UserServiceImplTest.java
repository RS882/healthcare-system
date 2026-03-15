package com.healthcare.user_service.service;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.exception.UserNotFoundException;
import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.UserRole;
import com.healthcare.user_service.model.dto.auth.UserAuthDto;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import com.healthcare.user_service.model.dto.request.RegistrationDto;
import com.healthcare.user_service.model.dto.response.RegistrationResponse;
import com.healthcare.user_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static com.healthcare.user_service.support.TestDataFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("registration()")
    class RegistrationTests {

        @Test
        @DisplayName("should normalize data, encode password, save user and return response")
        void shouldNormalizeEncodeSaveAndReturnResponse() {
            RegistrationDto inputDto = new RegistrationDto(
                    "  USER@Example.COM  ",
                    "  TsEt _uSEr^1  ",
                    "  secret123  "
            );

            when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");

            User savedUser = User.builder()
                    .id(1L)
                    .email("user@example.com")
                    .username("TsEt _uSEr^1")
                    .password("encoded-password")
                    .isActive(true)
                    .build();

            when(repository.saveAndFlush(any(User.class))).thenReturn(savedUser);

            RegistrationResponse result = userService.registration(inputDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo("user@example.com");
            assertThat(result.name()).isEqualTo("TsEt _uSEr^1");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(repository).saveAndFlush(userCaptor.capture());

            User userToSave = userCaptor.getValue();
            assertThat(userToSave.getEmail()).isEqualTo("user@example.com");
            assertThat(userToSave.getUsername()).isEqualTo("TsEt _uSEr^1");
            assertThat(userToSave.getPassword()).isEqualTo("encoded-password");
            assertThat(userToSave.isActive()).isTrue();
            assertThat(userToSave.getRoles()).hasSize(1);

            UserRole role = userToSave.getRoles().iterator().next();
            assertThat(role.getRole()).isEqualTo(Role.ROLE_PATIENT);
            assertThat(role.getUser()).isSameAs(userToSave);

            verify(passwordEncoder).encode("secret123");
        }
    }

    @Nested
    @DisplayName("getUserInfoByEmail()")
    class GetUserInfoByEmailTests {

        @Test
        @DisplayName("should normalize email and return auth dto")
        void shouldNormalizeEmailAndReturnDto() {
            Long userId = randomUserId();
            String userName =userName();
            User user = User.builder()
                    .id(userId)
                    .email("user@example.com")
                    .password("encoded-password")
                    .username(userName)
                    .isActive(true)
                    .build();

            UserRole role = UserRole.builder()
                    .user(user)
                    .role(roleUser())
                    .build();

            user.setRoles(Set.of(role));

            when(repository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

            UserAuthDto result = userService.getUserInfoByEmail("  USER@Example.COM  ");

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.email()).isEqualTo("user@example.com");
            assertThat(result.password()).isEqualTo("encoded-password");
            assertThat(result.enabled()).isTrue();
            assertThat(result.roles()).containsExactly(roleUser().name());

            verify(repository).findByEmail("user@example.com");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(repository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserInfoByEmail("  MISSING@example.com  "))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("missing@example.com");

            verify(repository).findByEmail("missing@example.com");
        }

        @ParameterizedTest(name = "should pass null to repository for email = [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n", "\t \n"})
        @DisplayName("should pass null to repository when email is blank")
        void shouldPassNullToRepositoryWhenEmailIsBlank(String email) {
            when(repository.findByEmail(null)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserInfoByEmail(email))
                    .isInstanceOf(UserNotFoundException.class);

            verify(repository).findByEmail(null);
        }
    }

    @Nested
    @DisplayName("getUserAuthInfoDtoById()")
    class GetUserAuthInfoDtoByIdTests {

        @ParameterizedTest(name = "should return null for invalid id = {0}")
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("should return null for non-positive ids")
        void shouldReturnNullForNonPositiveIds(Long userId) {
            UserAuthInfoDto result = userService.getUserAuthInfoDtoById(userId);

            assertThat(result).isNull();
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("should return null for null id")
        void shouldReturnNullForNullId() {
            UserAuthInfoDto result = userService.getUserAuthInfoDtoById(null);

            assertThat(result).isNull();
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("should return null when roles are null")
        void shouldReturnNullWhenRolesAreNull() {
            when(repository.findRolesByUserIdIfUserActive(5L)).thenReturn(null);

            UserAuthInfoDto result = userService.getUserAuthInfoDtoById(5L);

            assertThat(result).isNull();
            verify(repository).findRolesByUserIdIfUserActive(5L);
        }

        @Test
        @DisplayName("should return null when roles are empty")
        void shouldReturnNullWhenRolesAreEmpty() {
            when(repository.findRolesByUserIdIfUserActive(5L)).thenReturn(Set.of());

            UserAuthInfoDto result = userService.getUserAuthInfoDtoById(5L);

            assertThat(result).isNull();
            verify(repository).findRolesByUserIdIfUserActive(5L);
        }

        @Test
        @DisplayName("should return dto when roles exist")
        void shouldReturnDtoWhenRolesExist() {
            Set<Role> roles = Set.of(roleUser(), roleAdmin());
            Long userId = randomUserId();

            when(repository.findRolesByUserIdIfUserActive(userId)).thenReturn(roles);

            UserAuthInfoDto result = userService.getUserAuthInfoDtoById(userId);

            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.roles()).containsExactlyInAnyOrder(roleUser(), roleAdmin());

            verify(repository).findRolesByUserIdIfUserActive(userId);
        }
    }
}