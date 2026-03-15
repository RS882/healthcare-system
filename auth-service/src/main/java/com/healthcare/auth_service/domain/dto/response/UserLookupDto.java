package com.healthcare.auth_service.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO with user lookup information")
public record UserLookupDto(
        @Schema(description = "User email", example = "example@gmail.com")
        String userEmail
) {

}
