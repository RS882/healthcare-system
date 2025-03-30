package com.healthcare.user_service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO with user registration information")
public class RegistrationDto {

    @Schema(description = "User email", example = "example@gmail.com")
    @Email(
            message = "Email is not valid",
            regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
            flags = Pattern.Flag.CASE_INSENSITIVE
    )
    @NotNull(message = "Email cannot be null")
    private String userEmail;

    @Schema(description = "User name", example = "John")
    @NotBlank(message = "Username must not be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String userName;

    @Schema(description = "User password", example = "136Jkn!kPu5%")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{8,20}$",
            message = "Password should include at least one letter (A-Z or a-z)," +
                    " one digit (0-9), one special character (@, #, $, %, ^, &, +, =, !)," +
                    " have no spaces,no less than 8 characters and no more than 20"
    )
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
