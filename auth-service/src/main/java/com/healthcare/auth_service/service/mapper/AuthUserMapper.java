package com.healthcare.auth_service.service.mapper;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.UserInfoDto;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class AuthUserMapper {

    public static AuthUserDetails toAuthUser(UserInfoDto dto) {
        List<SimpleGrantedAuthority> authorities = dto.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new AuthUserDetails(
                dto.getId(),
                dto.getEmail(),
                dto.getPassword(),
                authorities,
                dto.isEnabled()
        );
    }
}
