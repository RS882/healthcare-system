package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.service.interfaces.PromptActivationTransactionalService;
import com.healthcare.aiservice.common.prompt.service.interfaces.TransactionConflictDetector;
import com.healthcare.aiservice.exception.AiPromptActivationRetryException;
import com.healthcare.aiservice.exception.AiPromptConcurrentActivationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import static com.healthcare.aiservice.config.PromptActivationRetryConfiguration.PROMPT_ACTIVATION_RETRY_TEMPLATE;

@Service
public class PromptActivationRetryExecutor {

    private final PromptActivationTransactionalService
            activationTransactionalService;

    private final TransactionConflictDetector
            transactionConflictDetector;

    private final RetryTemplate retryTemplate;

    public PromptActivationRetryExecutor(
            PromptActivationTransactionalService activationTransactionalService,
            MongoTransactionConflictDetector transactionConflictDetector,
            @Qualifier(PROMPT_ACTIVATION_RETRY_TEMPLATE)
            RetryTemplate retryTemplate
    ) {
        this.activationTransactionalService =
                activationTransactionalService;

        this.transactionConflictDetector =
                transactionConflictDetector;

        this.retryTemplate =
                retryTemplate;
    }

    public AiPromptDetailsResponse execute(
            String promptId
    ) {
        try {
            return retryTemplate.execute(
                    context -> activatePrompt(promptId)
            );

        } catch (AiPromptActivationRetryException exception) {
            throw new AiPromptConcurrentActivationException(
                    promptId,
                    exception
            );
        }
    }

    private AiPromptDetailsResponse activatePrompt(
            String promptId
    ) {
        try {
            return activationTransactionalService
                    .activatePrompt(promptId);

        } catch (RuntimeException exception) {
            if (transactionConflictDetector
                    .isTransientTransactionConflict(exception)) {

                throw new AiPromptActivationRetryException(
                        promptId,
                        exception
                );
            }

            throw exception;
        }
    }
}