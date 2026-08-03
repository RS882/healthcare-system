package com.healthcare.aiservice.exception;

import com.healthcare.aiservice.config.constant.FeatureName;
import org.springframework.http.HttpStatus;

public class AiPromptProviderNotFoundException extends RestException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    public AiPromptProviderNotFoundException(FeatureName feature) {

        super(STATUS,
                String.format("No prompt provider found for feature: '%s'", feature.name()),
        ErrorCode.AI_PROVIDER_NOT_FOUND);
    }
}
