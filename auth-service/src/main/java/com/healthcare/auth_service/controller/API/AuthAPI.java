package com.healthcare.auth_service.controller.API;

import com.healthcare.auth_service.config.annotation.bearer_token.BearerToken;
import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.AuthResponse;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.ValidationDto;
import com.healthcare.auth_service.exception_handler.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.auth_service.controller.API.ApiPaths.*;
import static com.healthcare.auth_service.service.constant.RefreshTokenTitle.REFRESH_TOKEN;

@RequestMapping(AUTH_BASIC_URL)
@Tag(name = "Authentication controller", description = "Controller for User authentication using JWT")
public interface AuthAPI {

    @Operation(
            summary = "Login of user",
            description = "Authenticates a user, returns AuthResponse " +
                    "with access token and user ID and set refresh token in cookie.",
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
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400LoginMissingField",
                                            ref = "#/components/examples/Error400LoginMissingField"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "401",
                    description = "Incorrect password or email",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error401LoginIncorrectField",
                                            ref = "#/components/examples/Error401LoginIncorrectField"
                                    )
                            }

                    )),
            @ApiResponse(responseCode = "403",
                    description = "The user does not have access",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error403LoginUserIsBlocked",
                                            ref = "#/components/examples/Error403LoginUserIsBlocked"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "404",
                    description = "The user is not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error404LoginUserIsNotFound",
                                            ref = "#/components/examples/Error404LoginUserIsNotFound"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "500",
                    description = "Temporary error during login",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500LoginTemporaryServiceError",
                                            ref = "#/components/examples/Error500LoginTemporaryServiceError"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "503",
                    description = "The server is currently overloaded or under maintenance.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error503LoginServiceUnavailable",
                                            ref = "#/components/examples/Error503LoginServiceUnavailable"
                                    )
                            }
                    ))
    }
    )
    @PostMapping(value = LOGIN)
    ResponseEntity<AuthResponse> login(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            LoginDto dto,
            @Parameter(hidden = true)
            HttpServletResponse response);

    @Operation(
            summary = "Refresh user's access and refresh token",
            description = "Refresh user's access and refresh token, returns AuthResponse " +
                    "with access token and user ID and set refresh token in cookie."
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
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400RefreshIncorrectCookie",
                                            ref = "#/components/examples/Error400RefreshIncorrectCookie"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "401",
                    description = "Invalid token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error401RefreshTokenIsIncorrect",
                                            ref = "#/components/examples/Error401RefreshTokenIsIncorrect"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "403",
                    description = "The user does not have access",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error403RefreshUserIsBlocked",
                                            ref = "#/components/examples/Error403RefreshUserIsBlocked"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "404",
                    description = "The user is not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error404RefreshUserIsNotFound",
                                            ref = "#/components/examples/Error404RefreshUserIsNotFound"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "500",
                    description = "Temporary error during refresh",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500RefreshTemporaryServiceError",
                                            ref = "#/components/examples/Error500RefreshTemporaryServiceError"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "503",
                    description = "The server is currently overloaded or under maintenance.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error503RefreshServiceUnavailable",
                                            ref = "#/components/examples/Error503RefreshServiceUnavailable"
                                    )
                            }
                    ))
    }
    )
    @PostMapping(value = REFRESH)
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
            @ApiResponse(responseCode = "400",
                    description = "Cookie is incorrect",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400LogoutIncorrectCookie",
                                            ref = "#/components/examples/Error400LogoutIncorrectCookie"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "401",
                    description = "Invalid token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error401LogoutTokenIsIncorrect",
                                            ref = "#/components/examples/Error401LogoutTokenIsIncorrect"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "500",
                    description = "Temporary error during logout",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500LogoutTemporaryServiceError",
                                            ref = "#/components/examples/Error500LogoutTemporaryServiceError"
                                    )
                            }
                    ))})
    @PostMapping(value = LOGOUT)
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
            String accessToken
    );

    @Operation(
            summary = "Access token Validation",
            description = "Validation access token. Access token must be in the header.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful validation",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationDto.class))}
            ),
            @ApiResponse(responseCode = "401",
                    description = "Invalid token",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error401ValidationTokenIsIncorrect",
                                            ref = "#/components/examples/Error401ValidationTokenIsIncorrect"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "404",
                    description = "The user is not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error404ValidationUserIsNotFound",
                                            ref = "#/components/examples/Error404ValidationUserIsNotFound"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "500",
                    description = "Temporary error during refresh",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500ValidationTemporaryServiceError",
                                            ref = "#/components/examples/Error500ValidationTemporaryServiceError"
                                    )
                            }
                    )),
            @ApiResponse(responseCode = "503",
                    description = "The server is currently overloaded or under maintenance.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error503ValidationServiceUnavailable",
                                            ref = "#/components/examples/Error503ValidationServiceUnavailable"
                                    )
                            }
                    ))})
    @GetMapping(value = VALIDATION)
    ResponseEntity<ValidationDto> validation(
            @AuthenticationPrincipal
            @Parameter(hidden = true)
            @NotNull
            AuthUserDetails principal);
}