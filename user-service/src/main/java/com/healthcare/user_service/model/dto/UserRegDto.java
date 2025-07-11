package com.healthcare.user_service.model.dto;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.UserRole;
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
public class UserRegDto {

    private Long id;

    private String email;

    private String name;

    private Set<Role> roles;

    private boolean enabled;

    public static UserRegDto getUserRegDto(User user) {
        return UserRegDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .roles(user.getRoles().stream()
                        .map(UserRole::getRole)
                        .collect(Collectors.toSet()))
                .enabled(user.isActive())
                .build();
    }
}
