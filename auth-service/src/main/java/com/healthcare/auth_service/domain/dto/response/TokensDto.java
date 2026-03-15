package com.healthcare.auth_service.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
public record TokensDto(
         String accessToken,
         String refreshToken,
         Long userId
) {

}
