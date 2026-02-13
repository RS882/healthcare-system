package com.healthcare.user_service.model.dto;

import com.healthcare.user_service.model.User;
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
@Schema(name = "Information of user for authorization")
public class UserAuthDto {

    @Schema(description = " ID of authorized user",
            example = "12")
    private Long id;

    @Schema(description = "User email", example = "example@gmail.com")
    private String email;

    @Schema(description = "User password", example = "136Jkn!kPu5%")
    private String password;

    @Schema(description = "User roles",
            example = "[ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN]")
    private Set<String> roles;

    @Schema(description = "Is user active?", example = "true")
    private boolean enabled;

    public static UserAuthDto getUserAuthDto(User user) {
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
}
