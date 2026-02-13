package com.healthcare.user_service.controller.API;

import com.healthcare.user_service.exception_handler.dto.ErrorResponse;
import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserAuthDto;
import com.healthcare.user_service.model.dto.UserDto;
import com.healthcare.user_service.model.dto.UserLookupDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.user_service.controller.API.ApiPaths.*;

@RequestMapping(USER_BASIC_URL)

@Tag(name = "User controller", description = "Controller for registration and CRUD operation of user")
public interface UserAPI {

    @Operation(summary = "Registration new user",
            description = "This method registers new user from RegistrationDto, returns UserDto " +
                    "with user information.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegistrationDto.class)))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Request is wrong",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400RegMissingField",
                                            ref = "#/components/examples/Error400RegMissingField"
                                    )
                            })),
            @ApiResponse(responseCode = "500",
                    description = "Temporary error when registering user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500RegTemporaryServiceError",
                                            ref = "#/components/examples/Error500RegTemporaryServiceError"
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
                                            name = "Error503RegServiceUnavailable",
                                            ref = "#/components/examples/Error503RegServiceUnavailable"
                                    )
                            }
                    ))
    })
    @PostMapping(REGISTRATION)
    ResponseEntity<UserDto> registerUser(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            RegistrationDto dto);

    @Operation(summary = "Get user for authorization",
            description = "This method get user information for authorization from UserLookupDto , returns UserAuthDto " +
                    "with full user information for authorization.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegistrationDto.class)))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request is successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserAuthDto.class))),

            @ApiResponse(responseCode = "400", description = "Request is wrong",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400LookupEmailIsWrong",
                                            ref = "#/components/examples/Error400LookupEmailIsWrong"
                                    )
                            })),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error404LookupUserIsNotFound",
                                            ref = "#/components/examples/Error404LookupUserIsNotFound"
                                    )
                            })),
            @ApiResponse(responseCode = "500",
                    description = "Temporary error when registering user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500LookupTemporaryServiceError",
                                            ref = "#/components/examples/Error500LookupTemporaryServiceError"
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
                                            name = "Error503LookupServiceUnavailable",
                                            ref = "#/components/examples/Error503LookupServiceUnavailable"
                                    )
                            }
                    ))
    })
    @PostMapping(LOOKUP)
    ResponseEntity<UserAuthDto> getUserAuth(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            UserLookupDto dto);


    //=====================================

    @GetMapping("/id/{id}")
    ResponseEntity<String> getUserInfoById(
            @NotNull
            @Positive
            @PathVariable Long id);
}
