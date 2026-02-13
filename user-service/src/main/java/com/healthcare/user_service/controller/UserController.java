package com.healthcare.user_service.controller;


import com.healthcare.user_service.controller.API.UserAPI;
import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserAuthDto;
import com.healthcare.user_service.model.dto.UserDto;
import com.healthcare.user_service.service.interfacies.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserAPI {

    private final UserService userService;

    @Override
    public ResponseEntity<UserDto> registerUser(
            @RequestBody
            RegistrationDto dto) {

        System.out.println();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registration(dto));
    }

    @Override
    public ResponseEntity<UserAuthDto> getUserInfoByEmail(
            @NotNull
            @Email
            @PathVariable String email) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserInfoByEmail(email));
    }

    @Override
    public ResponseEntity<String> getUserInfoById(Long id) {

        System.out.println();
        return ResponseEntity.status(HttpStatus.OK)
                .body(id.toString());
    }
}
