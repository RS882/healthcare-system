package com.healthcare.auth_service.domain.dto;

import lombok.*;
import jakarta.validation.constraints.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthDto {

    @NotNull(message = "User id cannot be null")
    private Long id;

    @Email(
            message = "Email is not valid",
            regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            flags = Pattern.Flag.CASE_INSENSITIVE
    )
    @NotNull(message = "Email cannot be null")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotEmpty(message = "Roles collection cannot be empty")
    @NotNull(message = "Roles cannot be null")
    private Set<String> roles;

    private boolean enabled;
}
