package com.healthcare.user_service.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(name = "General user information")
public record UserDto(

        @Schema(description = " ID of authorized user", example = "12")
        Long id,

        @Schema(description = "User email", example = "example@gmail.com")
        String email,

        @Schema(description = "User name", example = "John")
        String name,

        @Schema(description = "User roles",
                example = "[ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN]")
        Set<String> roles,

        @Schema(description = "Is user active?", example = "true")
        boolean enabled
) {
    public UserDto {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
