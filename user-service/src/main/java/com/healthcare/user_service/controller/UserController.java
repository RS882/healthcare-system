package com.healthcare.user_service.controller;


import com.healthcare.user_service.model.User;
import com.healthcare.user_service.service.interfacies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.registration(user));
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("✅ User Service is working!");
    }

}
