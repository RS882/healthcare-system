package com.healthcare.auth_service.service.feignClient;

import com.healthcare.auth_service.domain.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserClient {

    @PostMapping("/registration")
    UserDto registerUser(@RequestBody UserDto userDto);

    @GetMapping("/test")
    String getTest();
}
