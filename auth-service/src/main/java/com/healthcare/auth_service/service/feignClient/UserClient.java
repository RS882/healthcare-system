package com.healthcare.auth_service.service.feignClient;

import com.healthcare.auth_service.domain.dto.request.UserAuthDto;
import com.healthcare.auth_service.domain.dto.response.UserLookupDto;
import com.healthcare.auth_service.service.feignClient.config.CorrelationRequestIdFeignConfiguration;
import com.healthcare.auth_service.service.feignClient.config.InternalRequestFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        path = "/api/v1/users/internal",
        contextId = "internalUserServiceClient",
        configuration = {
                CorrelationRequestIdFeignConfiguration.class,
                InternalRequestFeignConfiguration.class
        }
)
public interface UserClient {

    @PostMapping(
            value = "/lookup",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    UserAuthDto lookupUser(
            @RequestBody UserLookupDto dto
    );
}



