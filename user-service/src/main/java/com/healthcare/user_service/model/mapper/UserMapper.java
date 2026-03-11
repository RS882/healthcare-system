package com.healthcare.user_service.model.mapper;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.UserRole;
import com.healthcare.user_service.model.dto.auth.UserAuthDto;
import com.healthcare.user_service.model.dto.request.RegistrationDto;


import com.healthcare.user_service.model.dto.response.RegistrationResponse;
import com.healthcare.user_service.model.dto.response.UserDto;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserAuthDto toUserAuthDto(User user) {
        return UserAuthDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .enabled(user.isActive())
                .roles(user.getRoles()
                        .stream()
                        .map(r -> r.getRole().name())
                        .collect(Collectors.toSet()))
                .build();
    }

    public static RegistrationResponse toRegistrationResponse(User user) {
        return new RegistrationResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername()
        );
    }

    public static UserDto toUserDto(User user) {

        Set<String> roles = user.getRoles()
                .stream()
                .map(r -> r.getRole().name())
                .collect(Collectors.toSet());

        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                roles,
                user.isActive()
        );
    }

    public static RegistrationDto normalizeRegistrationData(RegistrationDto dto) {
        if (dto == null) {
            return null;
        }

        String email = dto.userEmail();
        String normalizedEmail = StringUtils.hasText(email)
                ? email.strip().toLowerCase()
                : null;

        String userName = dto.userName();
        String normalizedUsername = StringUtils.hasText(userName)
                ? userName.strip()
                : null;

        String password = dto.password();
        String normalizedPassword = StringUtils.hasText(password)
                ? password.strip()
                : null;

        return new RegistrationDto(
                normalizedEmail,
                normalizedUsername,
                normalizedPassword
        );
    }

    public static User toUser(RegistrationDto dto, String encodedPassword, Role defaultRole) {
        User user = User.builder()
                .email(dto.userEmail())
                .password(encodedPassword)
                .username(dto.userName())
                .isActive(true)
                .build();

        UserRole role = UserRole.builder()
                .role(defaultRole)
                .user(user)
                .build();

        user.setRoles(Set.of(role));
        return user;
    }

}


