package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.RegistrationDto;
import com.healthcare.auth_service.domain.dto.TokensDto;
import com.healthcare.auth_service.service.feignClient.UserClient;
import com.healthcare.auth_service.service.interfacies.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    @Override
    public TokensDto registerUser(RegistrationDto dto) {
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        AuthUserDetails userDetails = userClient.registerUser(dto);

        return jwtService.getTokens(userDetails,userDetails.getId());
    }

    @Override
    public TokensDto loginUser(LoginDto dto) {
        var auth = new UsernamePasswordAuthenticationToken(dto.getUserEmail(), dto.getPassword());
        authManager.authenticate(auth);

        AuthUserDetails userDetails = userClient.getUserByEmail(dto.getUserEmail());

        return jwtService.getTokens(userDetails,userDetails.getId());
    }
}
