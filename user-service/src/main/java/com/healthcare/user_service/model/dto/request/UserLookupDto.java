package com.healthcare.user_service.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Schema(description = "DTO with user lookup information")
public record  UserLookupDto(
        @Schema(description = "User email", example = "example@gmail.com")
        @Email(
                message = "Email is not valid",
                regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
                flags = Pattern.Flag.CASE_INSENSITIVE
        )
        @NotNull(message = "Email cannot be null")
         String userEmail
) {
}
