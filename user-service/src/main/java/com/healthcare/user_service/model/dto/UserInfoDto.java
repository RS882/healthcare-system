package com.healthcare.user_service.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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
}
