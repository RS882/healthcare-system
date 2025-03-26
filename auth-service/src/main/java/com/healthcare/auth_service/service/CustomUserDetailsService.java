package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.exception_handler.exception.NotFoundException;
import com.healthcare.auth_service.service.feignClient.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String email) {

        try {
            AuthUserDetails user = userClient.getUserByEmail(email);
            return user;
        } catch (Exception e) {
            log.warn("User not found or error occurred for email {}: {}", email, e.getMessage());
            throw new NotFoundException("User not found: " + email);
        }
    }
}
