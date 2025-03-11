package com.healthcare.auth_service.controller;

import com.healthcare.auth_service.domain.model.User;
import com.healthcare.auth_service.service.interfacies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.registration(user));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("✅ Auth Service is working!");
    }

}
