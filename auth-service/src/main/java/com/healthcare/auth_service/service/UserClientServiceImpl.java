package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.RegistrationDto;
import com.healthcare.auth_service.domain.dto.UserInfoDto;
import com.healthcare.auth_service.exception_handler.exception.AccessDeniedException;
import com.healthcare.auth_service.exception_handler.exception.ServiceUnavailableException;
import com.healthcare.auth_service.exception_handler.exception.UserNotFoundException;
import com.healthcare.auth_service.service.feignClient.UserClient;
import com.healthcare.auth_service.service.interfacies.UserClientService;
import com.healthcare.auth_service.validator.UserInfoDtoValidator;
import feign.FeignException;
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
            if (!dto.isEnabled()) {
                log.warn("User {} has been blocked.", email);
                throw new AccessDeniedException("User " + email + " has been blocked.");
            }
        } catch (Exception e) {
            log.warn("User not found or error occurred for email {}: {}", email, e.getMessage());
            throw new UserNotFoundException(email, e);
        }
        validator.validateUser(dto);

        return toAuthUser(dto);
    }

    @Override
    public AuthUserDetails registerUser(RegistrationDto regDto) {

        UserInfoDto dto;
        try {
            dto = userClient.registerUser(regDto);
            if (dto == null) {
                log.warn("The user was not found in user service{}", regDto.getUserEmail());
                throw new UserNotFoundException(regDto.getUserEmail());
            }
        } catch (FeignException.NotFound e) {
            log.warn("The user was not found in user service{}", regDto.getUserEmail());
            throw new UserNotFoundException(regDto.getUserEmail(), e);
        } catch (Exception e) {
            log.warn("An error when registering a user through user-service. Email: {}, error: {}", regDto.getUserEmail(), e.toString(), e);
            throw new ServiceUnavailableException("Temporary error when registering user", e);
        }
        validator.validateUser(dto);

        return toAuthUser(dto);
    }
}
