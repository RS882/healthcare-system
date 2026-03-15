package com.healthcare.auth_service.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Login data", description = "User credentials")
public record LoginDto(
        @Schema(description = "User Email", example = "example@gmail.com")
        @Email(
                message = "Email is not valid",
                regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
                flags = Pattern.Flag.CASE_INSENSITIVE
        )
        @NotNull(message = "Email cannot be null")
        String userEmail,

        @Schema(description = "User password", example = "136Jkn!kPu5%")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{8,20}$",
                message = "Password should include at least one letter (A-Z or a-z)," +
                        " one digit (0-9), one special character (@, #, $, %, ^, &, +, =, !)," +
                        " have no spaces,no less than 8 characters and no more than 20"
        )
        @NotBlank(message = "Password cannot be empty")
        String password
) {
}
