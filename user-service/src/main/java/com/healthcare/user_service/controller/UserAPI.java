package com.healthcare.user_service.controller;

import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserInfoDto;
import com.healthcare.user_service.model.dto.UserRegDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/users")
public interface UserAPI {
//
//    @Operation(summary = "Registration new user",
//            description = "This method registers new user from RegistrationDto, returns AuthResponse " +
//                    "with access token and set refresh token in cookie.",
//            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = RegistrationDto.class)))
//    )
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "User registered successfully",
//                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = AuthResponse.class))),
//            @ApiResponse(responseCode = "400", description = "Request is wrong",
//                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ErrorResponse.class))),
//            @ApiResponse(responseCode = "404", description = "User not found",
//                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ErrorResponse.class))),
//            @ApiResponse(responseCode = "500",
//                    description = "Temporary error when registering user",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ErrorResponse.class)
//                    ))
//    })
    @PostMapping("/registration")
    ResponseEntity<UserRegDto> registerUser(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            RegistrationDto dto);

    @GetMapping("/email/{email}")
    public ResponseEntity<UserInfoDto> getUserInfoByEmail(
            @NotNull
            @Email
            @PathVariable String email);
}
