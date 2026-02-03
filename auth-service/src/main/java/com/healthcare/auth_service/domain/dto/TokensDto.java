package com.healthcare.auth_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class TokensDto {
    private String accessToken;
    private String refreshToken;
    private Long userId;
}
