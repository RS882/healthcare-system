package com.healthcare.user_service.service;


import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.UserRole;
import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserInfoDto;
import com.healthcare.user_service.repository.UserRepository;
import com.healthcare.user_service.service.interfacies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoDto getUserInfoByEmail(String email) {
        User user = repository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return getUserInfoDto(user);
    }

    @Override
    public UserInfoDto registration(RegistrationDto dto) {
        User user = repository.save(getUser(dto));
        return getUserInfoDto(user);
    }


    private UserInfoDto getUserInfoDto(User user) {
        return UserInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .enabled(user.isActive())
                .roles(user.getRoles()
                        .stream()
                        .map(r -> r.getRole().name())
                        .collect(Collectors.toSet()))
                .build();
    }

    private User getUser(RegistrationDto dto) {
        User user = User.builder()
                .email(dto.getUserEmail())
                .password(dto.getPassword())
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
