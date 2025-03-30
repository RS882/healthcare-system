package com.healthcare.auth_service.service.feignClient;

import com.healthcare.auth_service.domain.dto.UserInfoDto;
import com.healthcare.auth_service.exception_handler.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallback implements UserClient {
    @Override
    public UserInfoDto getUserByEmail(String email) {
        log.error("User service is unavailable. Cannot load user by email: {}", email);
        throw new ServiceUnavailableException("User service is temporarily unavailable");
    }

}
