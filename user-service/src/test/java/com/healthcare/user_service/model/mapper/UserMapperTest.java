package com.healthcare.user_service.model.mapper;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.UserRole;
import com.healthcare.user_service.model.dto.auth.UserAuthDto;
import com.healthcare.user_service.model.dto.request.RegistrationDto;
import com.healthcare.user_service.model.dto.response.RegistrationResponse;
import com.healthcare.user_service.model.dto.response.UserDto;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.healthcare.user_service.support.TestDataFactory.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class UserMapperTest {

    @Test
    void toUserAuthDto_shouldMapUserToUserAuthDto() {
        Long userId = randomUserId();
        String userEmail = userEmail();
        String userName = userName();
        String userPassword = "encoded-password";

        User user = User.builder()
                .id(userId)
                .email(userEmail)
                .password(userPassword)
                .username(userName)
                .isActive(true)
                .build();

        UserRole adminRole = UserRole.builder()
                .user(user)
                .role(roleAdmin())
                .build();

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(roleUser())
                .build();

        user.setRoles(Set.of(adminRole, userRole));

        UserAuthDto result = UserMapper.toUserAuthDto(user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo(userEmail);
        assertThat(result.password()).isEqualTo(userPassword);
        assertThat(result.enabled()).isTrue();
        assertThat(result.roles()).containsExactlyInAnyOrder(
                roleAdmin().name(),
                roleUser().name());
    }

    @Test
    void toRegistrationResponse_shouldMapUserToRegistrationResponse() {
        Long userId = randomUserId();
        String userEmail = userEmail();
        String userName = userName();

        User user = User.builder()
                .id(userId)
                .email(userEmail)
                .username(userName)
                .isActive(true)
                .build();

        RegistrationResponse result = UserMapper.toRegistrationResponse(user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo(userEmail);
        assertThat(result.name()).isEqualTo(userName);
    }

    @Test
    void toUserDto_shouldMapUserToUserDto() {
        Long userId = randomUserId();
        String userEmail = userEmail();
        String userName = userName();

        User user = User.builder()
                .id(userId)
                .email(userEmail)
                .username(userName)
                .isActive(true)
                .build();

        UserRole adminRole = UserRole.builder()
                .user(user)
                .role(roleAdmin())
                .build();

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(roleUser())
                .build();

        user.setRoles(Set.of(adminRole, userRole));

        UserDto result = UserMapper.toUserDto(user);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo(userEmail);
        assertThat(result.name()).isEqualTo(userName);
        assertThat(result.roles()).containsExactlyInAnyOrder(
                roleAdmin().name(),
                roleUser().name());
        assertThat(result.enabled()).isTrue();
    }

    @Test
    void normalizeRegistrationData_shouldReturnNull_whenDtoIsNull() {
        RegistrationDto result = UserMapper.normalizeRegistrationData(null);

        assertThat(result).isNull();
    }

    @Test
    void normalizeRegistrationData_shouldNormalizeFields_whenValuesContainSpaces() {

        RegistrationDto dto = new RegistrationDto(
                "  USER@Example.COM  ",
                "  TesT  _uSer!  ",
                "  secret123  "
        );

        RegistrationDto result = UserMapper.normalizeRegistrationData(dto);

        assertThat(result).isNotNull();
        assertThat(result.userEmail()).isEqualTo("user@example.com");
        assertThat(result.userName()).isEqualTo("TesT  _uSer!");
        assertThat(result.password()).isEqualTo("secret123");
    }

    @Test
    void normalizeRegistrationData_shouldSetNull_whenFieldsAreBlank() {
        RegistrationDto dto = new RegistrationDto(
                "   ",
                "\t  \n",
                " "
        );

        RegistrationDto result = UserMapper.normalizeRegistrationData(dto);

        assertThat(result).isNotNull();
        assertThat(result.userEmail()).isNull();
        assertThat(result.userName()).isNull();
        assertThat(result.password()).isNull();
    }

    @Test
    void normalizeRegistrationData_shouldKeepNullFieldsAsNull() {
        RegistrationDto dto = new RegistrationDto(
                null,
                null,
                null
        );

        RegistrationDto result = UserMapper.normalizeRegistrationData(dto);

        assertThat(result).isNotNull();
        assertThat(result.userEmail()).isNull();
        assertThat(result.userName()).isNull();
        assertThat(result.password()).isNull();
    }

    @Test
    void toUser_shouldCreateUserWithEncodedPasswordDefaultRoleAndActiveFlag() {

        String userEmail = userEmail();
        String userName = userName();
        String encodedPassword = "encoded-password";
        Role defaultRole = roleUser();

        RegistrationDto dto = new RegistrationDto(
                userEmail,
                userName,
                "raw-password"
        );

        User result = UserMapper.toUser(dto, encodedPassword, defaultRole);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(userEmail);
        assertThat(result.getUsername()).isEqualTo(userName);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getRoles()).hasSize(1);

        UserRole role = result.getRoles().iterator().next();
        assertThat(role.getRole()).isEqualTo(roleUser());
        assertThat(role.getUser()).isSameAs(result);
    }

    @Test
    void toUser_shouldUseProvidedDefaultRole() {
        RegistrationDto dto = new RegistrationDto(
                "admin@example.com",
                "admin",
                "password"
        );

        User result = UserMapper.toUser(dto, "encoded-admin-password", roleAdmin());

        assertThat(result.getRoles())
                .extracting(userRole -> userRole.getRole().name())
                .containsExactly(roleAdmin().name());
    }
}