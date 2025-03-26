package com.healthcare.auth_service.service.feignClient;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.RegistrationDto;

import com.healthcare.auth_service.exception_handler.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallback implements UserClient{
    @Override
    public AuthUserDetails getUserByEmail(String email) {
        log.error("User service is unavailable. Cannot load user by email: {}", email);
        throw new ServiceUnavailableException("User service is temporarily unavailable");
    }

    @Override
    public AuthUserDetails registerUser(RegistrationDto dto) {
        log.error("User service is unavailable. Cannot register user: {}", dto.getUserEmail());
        throw new ServiceUnavailableException("User service is temporarily unavailable");
    }
}
