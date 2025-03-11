package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.model.User;
import com.healthcare.auth_service.repository.UserRepository;
import com.healthcare.auth_service.service.interfacies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public User registration(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repository.save(user);
    }

    @Override
    public User findByUsername(String name) {
        return repository.findByUsername(name).orElseThrow(
                ()-> new RuntimeException("User not found")
        );
    }
}
