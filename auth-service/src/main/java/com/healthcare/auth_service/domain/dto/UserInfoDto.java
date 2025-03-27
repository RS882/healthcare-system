package com.healthcare.auth_service.domain.dto;

import lombok.*;
import jakarta.validation.constraints.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoDto {

    @NotNull(message = "user idd cannot be null")
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
    private Set<String> roles;

    private boolean enabled;
}
