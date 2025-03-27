package com.healthcare.user_service.controller;


import com.healthcare.user_service.model.User;
import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserInfoDto;
import com.healthcare.user_service.service.interfacies.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/email/{email}")
    public ResponseEntity<UserInfoDto> getUserInfoByEmail(
            @NotNull
            @Email
            @PathVariable String email) {

        return ResponseEntity.ok(userService.getUserInfoByEmail(email));
    }

    @GetMapping("/registration")
    public ResponseEntity<UserInfoDto> registration(
            @RequestBody
            RegistrationDto dto) {

        return ResponseEntity.ok(null);
    }

}
