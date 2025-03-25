package com.healthcare.auth_service.service.interfacies;

import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.RegistrationDto;
import com.healthcare.auth_service.domain.dto.TokensDto;

public interface AuthService {

    TokensDto registerUser(RegistrationDto dto);

    TokensDto loginUser(LoginDto dto);

}
