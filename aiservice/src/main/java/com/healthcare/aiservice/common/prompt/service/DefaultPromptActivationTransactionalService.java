package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.mapper.AiPromptMapper;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.service.interfaces.PromptActivationTransactionalService;
import com.healthcare.aiservice.exception.rest_exception.AiPromptNotFoundException;
import com.healthcare.aiservice.repository.AiPromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DefaultPromptActivationTransactionalService implements PromptActivationTransactionalService {

    private static final String SYSTEM_USER_ID = "system";
    private static final String SYSTEM_USERNAME = "system";

    private final AiPromptRepository repository;
    private final ActivePromptChangedEventPublisher publisher;

    @Override
    @Transactional
    public AiPromptDetailsResponse activatePrompt(
            String promptId
    ) {
        AiPrompt prompt = getPromptById(promptId);
        AiPromptKey key = AiPromptMapper.toKey(prompt);

        deactivateOtherActivePrompts(prompt, key);

        AiPrompt activatedPrompt = prompt.active()
                ? prompt
                : repository.save(
                prompt.activate(
                        SYSTEM_USER_ID,
                        SYSTEM_USERNAME
                )
        );

        publisher.publish(key);

        return AiPromptMapper.toDetailsResponse(
                activatedPrompt
        );
    }

    private AiPrompt getPromptById(String promptId) {
        return repository.findById(promptId)
                .orElseThrow(
                        () -> new AiPromptNotFoundException(
                                promptId
                        )
                );
    }

    private void deactivateOtherActivePrompts(
            AiPrompt prompt,
            AiPromptKey key
    ) {
        List<AiPrompt> activePrompts =
                repository
                        .findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                                key.feature(),
                                key.type(),
                                key.targetModel()
                        );

        activePrompts.stream()
                .filter(activePrompt ->
                        !Objects.equals(
                                activePrompt.id(),
                                prompt.id()
                        )
                )
                .map(activePrompt ->
                        activePrompt.deactivate(
                                SYSTEM_USER_ID,
                                SYSTEM_USERNAME
                        )
                )
                .forEach(repository::save);
    }
}