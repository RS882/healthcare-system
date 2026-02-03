package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.TokensDto;
import com.healthcare.auth_service.domain.dto.ValidationDto;
import com.healthcare.auth_service.exception_handler.exception.AccessDeniedException;
import com.healthcare.auth_service.exception_handler.exception.UnauthorizedException;
import com.healthcare.auth_service.service.interfacies.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.healthcare.auth_service.service.mapper.AuthUserMapper.toValidationDto;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserClientService userClientService;
    JwtService jwtService;
    AuthenticationManager authManager;
    RefreshTokenService refreshTokenService;
    BlockService blockService;
    TokenBlacklistService tokenBlacklistService;

    @Override
    public TokensDto login(LoginDto dto) {

        AuthUserDetails userDetails = userClientService.getUserByEmail(dto.getUserEmail());

        if (blockService.isBlocked(userDetails.getId())) {
            throw new AccessDeniedException("The limit of active sessions is exceeded. Try it later.");
        }

        var auth = new UsernamePasswordAuthenticationToken(dto.getUserEmail(), dto.getPassword());
        authManager.authenticate(auth);

        return generateAndStoreTokens(userDetails);

    }

    @Override
    public TokensDto refresh(String refreshToken) {

        AuthUserDetails userDetails = validateRefreshTokenAndGetUser(refreshToken);

        refreshTokenService.delete(refreshToken, userDetails.getId());

        return generateAndStoreTokens(userDetails);
    }

    @Override
    public void logout(String refreshToken, String accessToken) {

        if (StringUtils.hasText(accessToken)) {
            long ttl = jwtService.getRemainingTTLAccessToken(accessToken);
            tokenBlacklistService.blacklist(accessToken, ttl);
        }

        if (StringUtils.hasText(refreshToken)) {
            AuthUserDetails userDetails = validateRefreshTokenAndGetUser(refreshToken);
            refreshTokenService.delete(refreshToken, userDetails.getId());
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public ValidationDto getValidationDto(AuthUserDetails principal) {

        return toValidationDto(principal);
    }

    private TokensDto generateAndStoreTokens(AuthUserDetails userDetails) {

        Long userId = userDetails.getId();

        TokensDto tokens = jwtService.getTokens(userDetails, userId);
        refreshTokenService.save(tokens.getRefreshToken(), userId);
        return tokens;
    }

    private AuthUserDetails validateRefreshTokenAndGetUser(String refreshToken) {

        if (!StringUtils.hasText(refreshToken)) {
            throw new UnauthorizedException("No refresh token provided");
        }

        String email;
        try {
            email = jwtService.extractUserEmailFromRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token", e);
        }

        AuthUserDetails userDetails = userClientService.getUserByEmail(email);

        if (!jwtService.validateRefreshToken(refreshToken, userDetails)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        if (!refreshTokenService.isValid(refreshToken, userDetails.getId())) {
            throw new UnauthorizedException("Refresh token not found or revoked");
        }
        return userDetails;
    }
}