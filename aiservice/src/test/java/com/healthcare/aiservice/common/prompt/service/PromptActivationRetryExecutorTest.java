package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.service.interfaces.PromptActivationTransactionalService;
import com.healthcare.aiservice.common.prompt.service.interfaces.TransactionConflictDetector;
import com.healthcare.aiservice.exception.rest_exception.AiPromptConcurrentActivationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.retry.support.RetryTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Prompt activation retry executor tests: ")
class PromptActivationRetryExecutorTest {

    private static final String PROMPT_ID =
            "prompt-id";

    private final PromptActivationTransactionalService
            activationTransactionalService =
            mock(PromptActivationTransactionalService.class);

    private final TransactionConflictDetector
            transactionConflictDetector =
            mock(MongoTransactionConflictDetector.class);

    private final RetryTemplate retryTemplate =
            RetryTemplate.builder()
                    .maxAttempts(3)
                    .fixedBackoff(Duration.ofMillis(1))
                    .retryOn(
                            com.healthcare.aiservice.exception
                                    .AiPromptActivationRetryException.class
                    )
                    .build();

    private final PromptActivationRetryExecutor executor =
            new PromptActivationRetryExecutor(
                    activationTransactionalService,
                    (MongoTransactionConflictDetector) transactionConflictDetector,
                    retryTemplate
            );

    @Test
    void execute_ShouldRetryTransientConflict_AndReturnResult() {
        AiPromptDetailsResponse expectedResponse =
                mock(AiPromptDetailsResponse.class);

        RuntimeException mongoFailure =
                new RuntimeException("Mongo write conflict");

        when(activationTransactionalService
                .activatePrompt(PROMPT_ID))
                .thenThrow(mongoFailure)
                .thenThrow(mongoFailure)
                .thenReturn(expectedResponse);

        when(transactionConflictDetector
                .isTransientTransactionConflict(mongoFailure))
                .thenReturn(true);

        AiPromptDetailsResponse result =
                executor.execute(PROMPT_ID);

        assertThat(result)
                .isSameAs(expectedResponse);

        verify(
                activationTransactionalService,
                times(3)
        ).activatePrompt(PROMPT_ID);

        verify(
                transactionConflictDetector,
                times(2)
        ).isTransientTransactionConflict(mongoFailure);
    }

    @Test
    void execute_ShouldThrowConflict_WhenRetriesAreExhausted() {
        RuntimeException mongoFailure =
                new RuntimeException("Mongo write conflict");

        when(activationTransactionalService
                .activatePrompt(PROMPT_ID))
                .thenThrow(mongoFailure);

        when(transactionConflictDetector
                .isTransientTransactionConflict(mongoFailure))
                .thenReturn(true);

        assertThatThrownBy(
                () -> executor.execute(PROMPT_ID)
        )
                .isInstanceOf(
                        AiPromptConcurrentActivationException.class
                );

        verify(
                activationTransactionalService,
                times(3)
        ).activatePrompt(PROMPT_ID);

        verify(
                transactionConflictDetector,
                times(3)
        ).isTransientTransactionConflict(mongoFailure);
    }

    @Test
    void execute_ShouldNotRetryNonTransientException() {
        RuntimeException permanentFailure =
                new RuntimeException("Permanent failure");

        when(activationTransactionalService
                .activatePrompt(PROMPT_ID))
                .thenThrow(permanentFailure);

        when(transactionConflictDetector
                .isTransientTransactionConflict(permanentFailure))
                .thenReturn(false);

        assertThatThrownBy(
                () -> executor.execute(PROMPT_ID)
        )
                .isSameAs(permanentFailure);

        verify(
                activationTransactionalService,
                times(1)
        ).activatePrompt(PROMPT_ID);

        verify(transactionConflictDetector)
                .isTransientTransactionConflict(
                        permanentFailure
                );
    }
}