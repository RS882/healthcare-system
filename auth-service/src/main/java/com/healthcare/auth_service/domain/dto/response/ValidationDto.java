package com.healthcare.auth_service.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Schema(name = "User info after validation")
@Builder
public record ValidationDto(

        @Schema(description = "User ID",
                example = "215")
        Long userId,

        @Schema(description = "User roles",
                example = "[ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN]")
        Set<String> userRoles

) {
    public ValidationDto {
        userRoles = userRoles == null ? Set.of() : Set.copyOf(userRoles);
    }
}
