package com.healthcare.user_service.model.dto;

import com.healthcare.user_service.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthInfoDto {

    private Long id;
    private Set<Role> roles;
}
