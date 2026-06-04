package com.healthcare.aiservice.exception;


import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AI exception inspector tests: ")
class AiExceptionInspectorTest {

    private final AiExceptionInspector inspector = new AiExceptionInspector();

    @Test
    void findCause_ShouldReturnCause_WhenTargetExceptionExistsDirectly() {
        MismatchedInputException targetException =
                MismatchedInputException.from(null, String.class, "Invalid AI response");

        RuntimeException exception =
                new RuntimeException("Wrapper exception", targetException);

        Optional<MismatchedInputException> result =
                inspector.findCause(exception, MismatchedInputException.class);

        assertThat(result)
                .isPresent()
                .contains(targetException);
    }

    @Test
    void findCause_ShouldReturnCause_WhenTargetExceptionExistsDeepInCauseChain() {
        MismatchedInputException targetException =
                MismatchedInputException.from(null, String.class, "Invalid AI response");

        RuntimeException exception =
                new RuntimeException(
                        "Wrapper exception",
                        new IllegalArgumentException("Middle exception", targetException)
                );

        Optional<MismatchedInputException> result =
                inspector.findCause(exception, MismatchedInputException.class);

        assertThat(result)
                .isPresent()
                .contains(targetException);
    }

    @Test
    void findCause_ShouldReturnEmpty_WhenTargetExceptionDoesNotExist() {
        RuntimeException exception =
                new RuntimeException(
                        "Wrapper exception",
                        new IllegalArgumentException("Different cause")
                );

        Optional<MismatchedInputException> result =
                inspector.findCause(exception, MismatchedInputException.class);

        assertThat(result).isEmpty();
    }

    @Test
    void containsCause_ShouldReturnTrue_WhenTargetExceptionExists() {
        MismatchedInputException targetException =
                MismatchedInputException.from(null, String.class, "Invalid AI response");

        RuntimeException exception =
                new RuntimeException("Wrapper exception", targetException);

        boolean result =
                inspector.containsCause(exception, MismatchedInputException.class);

        assertThat(result).isTrue();
    }

    @Test
    void containsCause_ShouldReturnFalse_WhenTargetExceptionDoesNotExist() {
        RuntimeException exception =
                new RuntimeException(
                        "Wrapper exception",
                        new IllegalStateException("Different cause")
                );

        boolean result =
                inspector.containsCause(exception, MismatchedInputException.class);

        assertThat(result).isFalse();
    }
}