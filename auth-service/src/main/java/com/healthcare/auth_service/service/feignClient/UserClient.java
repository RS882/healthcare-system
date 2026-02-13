package com.healthcare.auth_service.service.feignClient;

import com.healthcare.auth_service.config.FeignRequestIdConfig;
import com.healthcare.auth_service.domain.dto.UserAuthDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        path = "/api/v1/users",
        fallback = UserClientFallback.class,
        configuration = FeignRequestIdConfig.class)
public interface UserClient {

    @GetMapping("/email/{email}")
    UserAuthDto getUserByEmail(
            @PathVariable("email") String email);
}


