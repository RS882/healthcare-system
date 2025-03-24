package com.healthcare.auth_service.service.feignClient;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserClient {

    @PostMapping("/registration")
    UserDto registerUser(@RequestBody UserDto userDto);

    @GetMapping("/test")
    String getTest();

    @GetMapping("/api/v1/users/email/{email}")
    AuthUserDetails getUserByEmail(@PathVariable("email") String email);
}
