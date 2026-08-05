package com.healthcare.aiservice.security.feign_client;

import com.healthcare.aiservice.security.dto.UserAuthInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        contextId = "userAuthInfoClient",
        path = "/api/v1/users/internal",
        configuration = UserServiceFeignConfiguration.class
)
public interface UserAuthInfoClient {

    @GetMapping("/{userId}/auth-info")
    UserAuthInfoDto getUserAuthInfo(
            @PathVariable("userId") long userId
    );
}
