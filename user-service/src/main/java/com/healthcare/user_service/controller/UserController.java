package com.healthcare.user_service.controller;


import com.healthcare.user_service.controller.API.UserAPI;
import com.healthcare.user_service.model.dto.auth.UserAuthDto;
import com.healthcare.user_service.model.dto.request.RegistrationDto;
import com.healthcare.user_service.model.dto.request.UserLookupDto;
import com.healthcare.user_service.model.dto.response.RegistrationResponse;
import com.healthcare.user_service.model.dto.response.UserDto;
import com.healthcare.user_service.service.interfacies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class UserController implements UserAPI {

    private final UserService userService;

    @Override
    public ResponseEntity<RegistrationResponse> registerUser(RegistrationDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.registration(dto));
    }

    @Override
    public ResponseEntity<UserAuthDto> getUserAuth(UserLookupDto dto) {

        String email = dto.userEmail();

        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserInfoByEmail(email));
    }

    @Override
    public ResponseEntity<UserDto> getUserInfoById(Long id) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(userService.getUserDtoById(id));
    }
}
