package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.RegistrationDto;
import com.healthcare.auth_service.domain.dto.TokensDto;
import com.healthcare.auth_service.service.feignClient.UserClient;
import com.healthcare.auth_service.service.interfacies.AuthService;
import com.healthcare.auth_service.service.interfacies.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    @Override
    public TokensDto registerUser(RegistrationDto dto) {

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        AuthUserDetails userDetails = userClient.registerUser(dto);

        return generateAndStoreTokens(userDetails);
    }

    @Override
    public TokensDto loginUser(LoginDto dto) {

        var auth = new UsernamePasswordAuthenticationToken(dto.getUserEmail(), dto.getPassword());
        authManager.authenticate(auth);

        AuthUserDetails userDetails = userClient.getUserByEmail(dto.getUserEmail());

        return generateAndStoreTokens(userDetails);
    }

    @Override
    public TokensDto refresh(HttpServletRequest request) {

        String refreshToken = cookieService.getRefreshTokenFromCookie(request);

        AuthUserDetails userDetails = validateRefreshTokenAndGetUser(refreshToken);

        TokensDto tokens = generateAndStoreTokens(userDetails);

        refreshTokenService.delete(refreshToken, userDetails.getId());

        return tokens;
    }

    @Override
    public void logout(HttpServletRequest request) {

        String refreshToken = cookieService.getRefreshTokenFromCookie(request);

        AuthUserDetails userDetails = validateRefreshTokenAndGetUser(refreshToken);

        refreshTokenService.delete(refreshToken, userDetails.getId());
    }

    private TokensDto generateAndStoreTokens(AuthUserDetails userDetails) {
        TokensDto tokens = jwtService.getTokens(userDetails, userDetails.getId());

        refreshTokenService.save(tokens.getRefreshToken(), userDetails.getId());

        return tokens;
    }

    public AuthUserDetails validateRefreshTokenAndGetUser(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("No refresh token provided");
        }

        String email = jwtService.extractUserEmailFromRefreshToken(refreshToken);

        AuthUserDetails userDetails = userClient.getUserByEmail(email);

        if (!jwtService.validateRefreshToken(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid or expired token");
        }

        if (!refreshTokenService.isValid(refreshToken, userDetails.getId())) {
            throw new RuntimeException("Refresh token not found");
        }
        return userDetails;
    }
}
