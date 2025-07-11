package com.healthcare.user_service.service;


import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.exception.UserNotFoundException;
import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.UserRole;
import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserInfoDto;
import com.healthcare.user_service.model.dto.UserRegDto;
import com.healthcare.user_service.repository.UserRepository;
import com.healthcare.user_service.service.interfacies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.healthcare.user_service.model.dto.UserInfoDto.getUserInfoDto;
import static com.healthcare.user_service.model.dto.UserRegDto.getUserRegDto;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoDto getUserInfoByEmail(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        return getUserInfoDto(user);
    }

    @Override
    public UserRegDto registration(RegistrationDto dto) {
        User user = repository.save(getUser(dto));
        return getUserRegDto(user);
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
