package com.healthcare.auth_service.service;

import com.healthcare.auth_service.service.interfacies.UserClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserClientService userClientService;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userClientService.getUserByEmail(email);
    }
}
