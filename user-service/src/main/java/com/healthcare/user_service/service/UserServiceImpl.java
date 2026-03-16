package com.healthcare.user_service.service;


import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.exception.UserNotFoundException;
import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.dto.auth.UserAuthDto;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import com.healthcare.user_service.model.dto.request.RegistrationDto;
import com.healthcare.user_service.model.dto.response.RegistrationResponse;
import com.healthcare.user_service.model.dto.response.UserDto;
import com.healthcare.user_service.model.mapper.UserMapper;
import com.healthcare.user_service.repository.UserRepository;
import com.healthcare.user_service.service.interfacies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

import static com.healthcare.user_service.model.mapper.UserMapper.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    private final Role defaultRole = Role.ROLE_PATIENT;

    @Override
    public RegistrationResponse registration(RegistrationDto dto) {

        RegistrationDto prepareRegistrationDto = normalizeRegistrationData(dto);

        String encodedPassword = passwordEncoder.encode(prepareRegistrationDto.password());

        User newUser = toUser(prepareRegistrationDto, encodedPassword, defaultRole);

        User savedUser = repository.saveAndFlush(newUser);

        return toRegistrationResponse(savedUser);
    }

    @Override
    public UserAuthDto getUserInfoByEmail(String email) {
        String normalizedEmail = StringUtils.hasText(email) ?
                email.toLowerCase().strip() :
                null;
        User user = repository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException(normalizedEmail));

        return toUserAuthDto(user);
    }

    @Override
    public UserAuthInfoDto getUserAuthInfoDtoById(Long userId) {

        if (userId == null || userId <= 0) {
            return null;
        }

        Set<Role> roles = repository.findRolesByUserIdIfUserActive(userId);

        if (roles == null || roles.isEmpty()) {
            return null;
        }

        return new UserAuthInfoDto(userId, roles);
    }

    @Override
    public UserDto getUserDtoById(Long id) {

        return repository.findById(id)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
