package com.healthcare.user_service.model.dto.auth;

import com.healthcare.user_service.constant.Role;

import java.util.Set;

public record UserAuthInfoDto(
        Long userId,
        Set<Role> roles
) {
    public UserAuthInfoDto {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
