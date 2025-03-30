package com.healthcare.user_service.model.dto;

import com.healthcare.user_service.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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
}
