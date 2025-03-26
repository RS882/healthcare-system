package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.RegistrationDto;
import com.healthcare.auth_service.domain.dto.TokensDto;
import com.healthcare.auth_service.domain.dto.UserInfoDto;
import com.healthcare.auth_service.exception_handler.exception.AccessDeniedException;
import com.healthcare.auth_service.exception_handler.exception.UnauthorizedException;
import com.healthcare.auth_service.service.feignClient.UserClient;
import com.healthcare.auth_service.service.interfacies.AuthService;
import com.healthcare.auth_service.service.interfacies.BlockService;
import com.healthcare.auth_service.service.interfacies.RefreshTokenService;
import com.healthcare.auth_service.service.interfacies.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import static com.healthcare.auth_service.service.mapper.AuthUserMapper.toAuthUser;
import static com.healthcare.auth_service.service.token_utilities.TokenUtilities.extractJwtFromRequest;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserClient userClient;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    AuthenticationManager authManager;
    RefreshTokenService refreshTokenService;
    CookieService cookieService;
    BlockService blockService;
    TokenBlacklistService tokenBlacklistService;

    @Override
    public TokensDto registeration(RegistrationDto dto) {

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        UserInfoDto userDetails = userClient.registerUser(dto);

        return generateAndStoreTokens(toAuthUser(userDetails));
    }

    @Override
    public TokensDto login(LoginDto dto) {

        AuthUserDetails userDetails = toAuthUser(userClient.getUserByEmail(dto.getUserEmail()));

        if (blockService.isBlocked(userDetails.getId())) {
            throw new AccessDeniedException("The limit of active sessions is exceeded. Try it later.");
        }

        var auth = new UsernamePasswordAuthenticationToken(dto.getUserEmail(), dto.getPassword());
        authManager.authenticate(auth);

        return generateAndStoreTokens(userDetails);

    }

    @Override
    public TokensDto refresh(HttpServletRequest request) {

        String refreshToken = cookieService.getRefreshTokenFromCookie(request);

        AuthUserDetails userDetails = validateRefreshTokenAndGetUser(refreshToken);

        refreshTokenService.delete(refreshToken, userDetails.getId());

        return generateAndStoreTokens(userDetails);
    }

    @Override
    public void logout(HttpServletRequest request) {

        String accessToken = extractJwtFromRequest(request);

        if (StringUtils.hasText(accessToken)) {
            long ttl = jwtService.getRemainingTTLAccessToken(accessToken);
            tokenBlacklistService.blacklist(accessToken, ttl);
        }

        String refreshToken = cookieService.getRefreshTokenFromCookie(request);

        if (StringUtils.hasText(refreshToken)) {
            AuthUserDetails userDetails = validateRefreshTokenAndGetUser(refreshToken);
            refreshTokenService.delete(refreshToken, userDetails.getId());
        }
        SecurityContextHolder.clearContext();
    }

    private TokensDto generateAndStoreTokens(AuthUserDetails userDetails) {
        TokensDto tokens = jwtService.getTokens(userDetails, userDetails.getId());

        refreshTokenService.save(tokens.getRefreshToken(), userDetails.getId());

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
            throw new UnauthorizedException("Invalid refresh token");
        }

        AuthUserDetails userDetails = toAuthUser(userClient.getUserByEmail(email));

        if (!jwtService.validateRefreshToken(refreshToken, userDetails)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        if (!refreshTokenService.isValid(refreshToken, userDetails.getId())) {
            throw new UnauthorizedException("Refresh token not found or revoked");
        }
        return userDetails;
    }
}