package com.healthcare.user_service.service;


import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.exception.UserNotFoundException;
import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.UserRole;
import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserAuthDto;
import com.healthcare.user_service.model.dto.UserDto;
import com.healthcare.user_service.repository.UserRepository;
import com.healthcare.user_service.service.interfacies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.healthcare.user_service.model.dto.UserAuthDto.getUserAuthDto;
import static com.healthcare.user_service.model.dto.UserDto.getUserDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserAuthDto getUserInfoByEmail(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        return getUserAuthDto(user);
    }

    @Override
    public UserDto registration(RegistrationDto dto) {
        User user = repository.saveAndFlush(getUser(dto));
        return getUserDto(user);
    }

    private User getUser(RegistrationDto dto) {
        User user = User.builder()
                .email(dto.getUserEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .username(dto.getUserName())
                .isActive(true)
                .build();

        UserRole role = UserRole.builder()
                .role(Role.ROLE_PATIENT)
                .user(user)
                .build();

        user.setRoles(Set.of(role));
        return user;
    }
}
