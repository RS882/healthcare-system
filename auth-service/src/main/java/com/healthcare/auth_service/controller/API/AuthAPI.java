package com.healthcare.auth_service.controller.API;

import com.healthcare.auth_service.config.annotation.bearer_token.BearerToken;
import com.healthcare.auth_service.domain.dto.AuthResponse;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.RegistrationDto;
import com.healthcare.auth_service.exception_handler.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.auth_service.service.CookieService.REFRESH_TOKEN;

@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication controller", description = "Controller for User registration, authentication using JWT")
public interface AuthAPI {

    @Operation(summary = "Registration new user",
            description = "This method registers new user from RegistrationDto, returns AuthResponse " +
                    "with access token and set refresh token in cookie.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegistrationDto.class)))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request is wrong",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Temporary error when registering user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))
    })
    @PostMapping("/registration")
    ResponseEntity<AuthResponse> registerUser(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            RegistrationDto dto,
            @Parameter(hidden = true)
            HttpServletResponse response);

    @Operation(
            summary = "Login of user",
            description = "Authenticates a user, returns AuthResponse " +
                    "with access token and set refresh token in cookie.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginDto.class)))
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful login",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))}
            ),
            @ApiResponse(responseCode = "400",
                    description = "Request is wrong",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "401",
                    description = "Incorrect password or email",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)

                    )),
            @ApiResponse(responseCode = "403",
                    description = "The user does not have access",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "500",
                    description = "Temporary error during login",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))}
    )
    @PostMapping("/login")
    ResponseEntity<AuthResponse> loginUser(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            LoginDto dto,
            @Parameter(hidden = true)
            HttpServletResponse response);

    @Operation(
            summary = "Refresh user's access and refresh token",
            description = "Refresh user's access and refresh token, returns AuthResponse " +
                    "with access token and set refresh token in cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful refresh",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))}
            ),
            @ApiResponse(responseCode = "400",
                    description = "Cookie is incorrect",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    )),
            @ApiResponse(responseCode = "401",
                    description = "Invalid token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))}
    )
    @PostMapping("/refresh")
    ResponseEntity<AuthResponse> refresh(
            @Parameter(hidden = true)
            HttpServletResponse response,
            @Parameter(
                    in = ParameterIn.COOKIE,
                    name = REFRESH_TOKEN,
                    required = true,
                    hidden = true,
                    schema = @Schema(type = "string")
            )
            @CookieValue(name = REFRESH_TOKEN)
            @NotNull
            String refreshToken);

    @Operation(
            summary = "Logout of user",
            description = "Logout of user. Remove the refresh token from cookie " +
                    "and and storage. Access token is placed in blacklist")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Successful logout"
            ),
            @ApiResponse(responseCode = "401",
                    description = "Invalid token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                    ))})
    ResponseEntity<Void> logout(
            @Parameter(hidden = true)
            HttpServletResponse response,
            @Parameter(
                    in = ParameterIn.COOKIE,
                    name = REFRESH_TOKEN,
                    required = true,
                    hidden = true,
                    schema = @Schema(type = "string")
            )
            @CookieValue(name = REFRESH_TOKEN)
            @NotNull
            String refreshToken,
            @BearerToken
            @Parameter(hidden = true)
            @NotNull
            String accessToken);
}
