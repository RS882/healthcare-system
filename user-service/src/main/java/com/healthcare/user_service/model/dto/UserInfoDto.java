package com.healthcare.user_service.model.dto;

import com.healthcare.user_service.model.User;
import jakarta.validation.constraints.*;
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
public class UserInfoDto {

    private Long id;

    private String email;

    private String password;

    private Set<String> roles;

    private boolean enabled;

    public static UserInfoDto getUserInfoDto(User user) {
        return UserInfoDto.builder()
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
