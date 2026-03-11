package com.healthcare.user_service.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Builder
@Schema(name = "Information of user for authorization")
public record UserAuthDto(
        @Schema(description = " ID of authorized user",
                example = "12")
        Long id,

        @Schema(description = "User email", example = "example@gmail.com")
        String email,

        @Schema(description = "User password", example = "136Jkn!kPu5%")
        String password,

        @Schema(description = "User roles",
                example = "[ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN]")
        Set<String> roles,

        @Schema(description = "Is user active?", example = "true")
        boolean enabled
) {
    public UserAuthDto {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
    }
}
