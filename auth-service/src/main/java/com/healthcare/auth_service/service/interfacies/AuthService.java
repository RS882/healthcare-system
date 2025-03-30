package com.healthcare.auth_service.service.interfacies;

import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.TokensDto;

public interface AuthService {

    TokensDto login(LoginDto dto);

    TokensDto refresh(String refreshToken);

    void logout(String refreshToken, String accessToken);
}
