package com.healthcare.auth_service.service.mapper;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.UserAuthDto;
import com.healthcare.auth_service.domain.dto.ValidationDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthUserMapper {

    public static AuthUserDetails toAuthUser(UserAuthDto dto) {

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

    public static ValidationDto toValidationDto(AuthUserDetails principal) {

        Set<String> authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return ValidationDto.builder()
                .userId(principal.getId())
                .userRoles(authorities)
                .build();
    }
}
