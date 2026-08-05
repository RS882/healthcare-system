package com.healthcare.aiservice.security.feign_client;

import com.healthcare.aiservice.exception.UserAuthInfoNotFoundException;
import com.healthcare.aiservice.exception.UserServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class UserServiceFeignErrorDecoder
        implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder =
            new ErrorDecoder.Default();

    @Override
    public Exception decode(
            String methodKey,
            Response response
    ) {
        int status = response.status();

        return switch (status) {

            case 401, 403, 404 ->
                    new UserAuthInfoNotFoundException();

            case 500, 502, 503, 504 ->
                    new UserServiceUnavailableException(
                            "User service request failed with status %d"
                                    .formatted(status)
                    );

            default ->
                    defaultDecoder.decode(
                            methodKey,
                            response
                    );
        };
    }
}