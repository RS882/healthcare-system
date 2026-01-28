package com.healthcare.auth_service.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
@Schema(name = "User info after validation")
@Builder
public class ValidationDto {

    @Schema(description = "User ID",
            example = "215")
    private Long userId;

    @Schema(description = "User roles",
            example = "[ROLE_PATIENT, ROLE_DOCTOR, ROLE_ADMIN]")
    private Set<String> userRoles;

}
