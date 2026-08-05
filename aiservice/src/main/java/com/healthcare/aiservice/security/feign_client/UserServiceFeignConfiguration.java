package com.healthcare.aiservice.security.feign_client;


import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class UserServiceFeignConfiguration {

    @Bean
    public ErrorDecoder userServiceFeignErrorDecoder() {
        return new UserServiceFeignErrorDecoder();
    }
}
