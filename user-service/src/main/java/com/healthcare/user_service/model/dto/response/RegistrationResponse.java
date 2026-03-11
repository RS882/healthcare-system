package com.healthcare.user_service.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(name = "User information after registration")
public record RegistrationResponse(

        @Schema(description = " ID of authorized user", example = "12")
        Long id,

        @Schema(description = "User email", example = "example@gmail.com")
        String email,

        @Schema(description = "User name", example = "John")
        String name
) {

}
