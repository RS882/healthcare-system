package com.healthcare.aiservice.security.dto;



import com.healthcare.aiservice.security.constant.Role;

import java.util.Set;

public record UserAuthInfoDto(
        Long userId,
        Set<Role> roles
) {
    public UserAuthInfoDto {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
