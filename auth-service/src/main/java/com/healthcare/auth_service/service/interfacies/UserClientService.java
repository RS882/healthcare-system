package com.healthcare.auth_service.service.interfacies;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.RegistrationDto;

public interface UserClientService {

    AuthUserDetails getUserByEmail(String email);

    AuthUserDetails registerUser(RegistrationDto dto);
}
