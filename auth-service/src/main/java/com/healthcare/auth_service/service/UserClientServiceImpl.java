package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.UserInfoDto;
import com.healthcare.auth_service.exception_handler.exception.AccessDeniedException;
import com.healthcare.auth_service.exception_handler.exception.UserNotFoundException;
import com.healthcare.auth_service.service.feignClient.UserClient;
import com.healthcare.auth_service.service.interfacies.RequestIdService;
import com.healthcare.auth_service.service.interfacies.UserClientService;
import com.healthcare.auth_service.validator.UserInfoDtoValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.healthcare.auth_service.service.mapper.AuthUserMapper.toAuthUser;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserClientServiceImpl implements UserClientService {

    UserClient userClient;
    UserInfoDtoValidator validator;

    @Override
    public AuthUserDetails getUserByEmail(String email) {
        UserInfoDto dto;
        try {
            dto = userClient.getUserByEmail(email);
        } catch (Exception e) {
            log.warn("User not found or error occurred for email {}: {}", email, e.getMessage());
            throw new UserNotFoundException(email, e);
        }
        if (dto == null) {
            log.warn("User for email {} not found. User service get null", email);
            throw new UserNotFoundException(email);
        }
        if (!dto.isEnabled()) {
            log.warn("User {} has been blocked.", email);
            throw new AccessDeniedException("User " + email + " has been blocked.");
        }
        validator.validateUser(dto);

        return toAuthUser(dto);
    }
}
