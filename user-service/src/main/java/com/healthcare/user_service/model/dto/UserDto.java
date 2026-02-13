package com.healthcare.user_service.model.dto;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "General user information")
public class UserDto {

    @Schema(description = " ID of authorized user",
            example = "12")
    private Long id;

    @Schema(description = "User email", example = "example@gmail.com")
    private String email;

    @Schema(description = "User name", example = "John")
    private String name;

    @Schema(description = "User roles",
            example = "[ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN]")
    private Set<String> roles;

    @Schema(description = "Is user active?", example = "true")
    private boolean enabled;

    public static UserDto getUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .roles(user.getRoles()
                        .stream()
                        .map(r -> r.getRole().name())
                        .collect(Collectors.toSet()))
                .enabled(user.isActive())
                .build();
    }
}
