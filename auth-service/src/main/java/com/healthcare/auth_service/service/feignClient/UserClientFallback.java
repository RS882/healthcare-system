package com.healthcare.auth_service.service.feignClient;

import com.healthcare.auth_service.domain.dto.request.UserAuthDto;
import com.healthcare.auth_service.domain.dto.response.UserLookupDto;
import com.healthcare.auth_service.exception_handler.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallback implements UserClient {
    @Override
    public UserAuthDto lookupUser(UserLookupDto dto) {
        log.error("User service is unavailable. Cannot load user by email: {}", dto.userEmail());
        throw new ServiceUnavailableException("User service is temporarily unavailable");
    }

}
