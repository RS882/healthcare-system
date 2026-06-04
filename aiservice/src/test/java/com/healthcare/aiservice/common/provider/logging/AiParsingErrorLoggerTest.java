package com.healthcare.aiservice.common.provider.logging;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.exception.AiExceptionInspector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AI parsing error logger tests: ")
class AiParsingErrorLoggerTest {

    private final AiExceptionInspector exceptionInspector =
            new AiExceptionInspector();

    private final AiParsingErrorLogger logger =
            new AiParsingErrorLogger(exceptionInspector);

    @Test
    void logIfParsingError_ShouldNotThrow_WhenExceptionContainsMismatchedInputException() {
        MismatchedInputException parsingException =
                MismatchedInputException.from(null, String.class, "Invalid AI response");

        RuntimeException exception =
                new RuntimeException("AI response conversion failed", parsingException);

        assertThatCode(() -> logger.logIfParsingError(
                exception,
                MedicalSummaryResponse.class
        )).doesNotThrowAnyException();
    }

    @Test
    void logIfParsingError_ShouldNotThrow_WhenExceptionDoesNotContainMismatchedInputException() {
        RuntimeException exception =
                new RuntimeException(
                        "AI provider failed",
                        new IllegalStateException("Connection error")
                );

        assertThatCode(() -> logger.logIfParsingError(
                exception,
                MedicalSummaryResponse.class
        )).doesNotThrowAnyException();
    }
}