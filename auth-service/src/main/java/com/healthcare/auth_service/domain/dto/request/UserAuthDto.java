package com.healthcare.auth_service.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.Set;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserAuthDto(
        @NotNull(message = "User id cannot be null")
        Long id,

        @Email(
                message = "Email is not valid",
                regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
                flags = Pattern.Flag.CASE_INSENSITIVE
        )
        @NotNull(message = "Email cannot be null")
        String email,

        @NotBlank(message = "Password cannot be empty")
        String password,

        @NotEmpty(message = "Roles collection cannot be empty")
        @NotNull(message = "Roles cannot be null")
        Set<String> roles,

        boolean enabled) {
        public UserAuthDto {
                roles = roles == null ? Set.of() : Set.copyOf(roles);
        }
}
