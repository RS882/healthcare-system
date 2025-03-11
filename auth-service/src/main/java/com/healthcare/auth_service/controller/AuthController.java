package com.healthcare.auth_service.controller;

import com.healthcare.auth_service.service.interfacies.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;


    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("✅ Auth Service is working!");
    }

    @GetMapping("/test/user")
    public ResponseEntity<String> testUser() {
        return ResponseEntity.ok(authService.getUserTest());
    }

}
