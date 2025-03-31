package com.healthcare.auth_service.controller;

import com.healthcare.auth_service.controller.API.AuthAPI;
import com.healthcare.auth_service.domain.dto.AuthResponse;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.service.CookieService;
import com.healthcare.auth_service.service.interfacies.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthAPI {

    private final AuthService authService;
    private final CookieService cookieService;

    @Override
    public ResponseEntity<AuthResponse> login(
            LoginDto dto,
            HttpServletResponse response) {

        var tokens = authService.login(dto);
        cookieService.setRefreshTokenToCookie(response, tokens.getRefreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new AuthResponse(tokens.getAccessToken()));
    }

    @Override
    public ResponseEntity<AuthResponse> refresh(
            HttpServletResponse response,
            String refreshToken) {
        var tokens = authService.refresh(refreshToken);
        cookieService.setRefreshTokenToCookie(response, tokens.getRefreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new AuthResponse(tokens.getAccessToken()));
    }

    @Override
    public ResponseEntity<Void> logout(
            HttpServletResponse response,
            String refreshToken,
            String accessToken) {
        authService.logout(refreshToken, accessToken);
        cookieService.removeRefreshTokenFromCookie(response);

        return ResponseEntity.noContent().build();
    }
}
