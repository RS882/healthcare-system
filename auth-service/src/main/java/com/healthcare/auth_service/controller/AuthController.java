package com.healthcare.auth_service.controller;

import com.healthcare.auth_service.domain.dto.AuthResponse;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.RegistrationDto;
import com.healthcare.auth_service.service.CookieService;
import com.healthcare.auth_service.service.interfacies.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/registration")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid
            @RequestBody
            RegistrationDto dto,
            HttpServletResponse response) {

        var tokens = authService.registeration(dto);
        cookieService.setRefreshTokenToCookie(response, tokens.getRefreshToken());

        return ResponseEntity.ok().body(new AuthResponse(tokens.getAccessToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(
            @Valid
            @RequestBody
            LoginDto dto,
            HttpServletResponse response) {

        var tokens = authService.login(dto);
        cookieService.setRefreshTokenToCookie(response, tokens.getRefreshToken());

        return ResponseEntity.ok().body(new AuthResponse(tokens.getAccessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        var tokens = authService.refresh(request);
        cookieService.setRefreshTokenToCookie(response, tokens.getRefreshToken());

        return ResponseEntity.ok().body(new AuthResponse(tokens.getAccessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        authService.logout(request);
        cookieService.removeRefreshTokenFromCookie(response);

        return ResponseEntity.noContent().build();
    }

}
