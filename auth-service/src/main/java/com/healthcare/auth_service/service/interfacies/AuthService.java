package com.healthcare.auth_service.service.interfacies;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.TokensDto;
import com.healthcare.auth_service.domain.dto.ValidationDto;

public interface AuthService {

    TokensDto login(LoginDto dto);

    TokensDto refresh(String refreshToken);

    void logout(String refreshToken, String accessToken);

    ValidationDto getValidationDto(AuthUserDetails principal);
}
