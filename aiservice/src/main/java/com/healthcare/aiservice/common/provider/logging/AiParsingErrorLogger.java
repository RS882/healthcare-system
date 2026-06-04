package com.healthcare.aiservice.common.provider.logging;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import com.healthcare.aiservice.exception.AiExceptionInspector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiParsingErrorLogger {

    private final AiExceptionInspector exceptionInspector;

    public void logIfParsingError(
            RuntimeException exception,
            Class<?> responseType
    ) {
        exceptionInspector
                .findCause(exception, MismatchedInputException.class)
                .ifPresent(cause -> log.warn(
                        "AI response parsing failed. responseType={}, error={}",
                        responseType.getSimpleName(),
                        cause.getOriginalMessage(),
                        exception
                ));
    }
}
