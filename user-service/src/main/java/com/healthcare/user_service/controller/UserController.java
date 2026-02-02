package com.healthcare.user_service.controller;


import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserInfoDto;
import com.healthcare.user_service.model.dto.UserRegDto;
import com.healthcare.user_service.service.interfacies.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController implements UserAPI {

    private final UserService userService;

    @Override
    @GetMapping("/email/{email}")
    public ResponseEntity<UserInfoDto> getUserInfoByEmail(
            @NotNull
            @Email
            @PathVariable String email) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserInfoByEmail(email));
    }

    @Override
    @GetMapping("/id/{id}")
    public ResponseEntity<UserInfoDto> getUserInfoById(String email) {
        return null;
    }

    @Override
    @PostMapping("/registration")
    public ResponseEntity<UserRegDto> registerUser(
            @RequestBody
            RegistrationDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registration(dto));
    }
}
