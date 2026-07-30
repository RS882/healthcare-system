package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.dto.AiPromptResponse;
import com.healthcare.aiservice.common.prompt.dto.CreateAiPromptRequest;
import com.healthcare.aiservice.common.prompt.mapper.AiPromptMapper;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.normalizer.PromptTextNormalizer;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptManagementService;
import com.healthcare.aiservice.exception.AiActivePromptNotFoundException;
import com.healthcare.aiservice.exception.AiPromptNotFoundException;
import com.healthcare.aiservice.exception.AiPromptStateInvalidException;
import com.healthcare.aiservice.exception.AiPromptVersionConflictException;
import com.healthcare.aiservice.repository.AiPromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultAiPromptManagementService implements AiPromptManagementService {

    private static final String SYSTEM_USER_ID = "system";
    private static final String SYSTEM_USERNAME = "system";

    private final AiPromptRepository repository;
    private final PromptTextNormalizer normalizer;
    private final ActivePromptChangedEventPublisher publisher;

    @Override
    public AiPromptDetailsResponse createPrompt(CreateAiPromptRequest request) {

        AiPromptKey aiPromptKey = AiPromptMapper.toKey(request);

        long nextVersion = resolveNextVersion(aiPromptKey);

        AiPrompt prompt = buildPrompt(request, nextVersion);

        try {
            AiPrompt savedPrompt = repository.save(prompt);

            return AiPromptMapper.toDetailsResponse(savedPrompt);

        } catch (DuplicateKeyException ex) {
            throw new AiPromptVersionConflictException(
                    aiPromptKey,
                    nextVersion);
        }
    }

    @Override
    @Transactional
    public AiPromptDetailsResponse activatePrompt(String promptId) {
        AiPrompt prompt = getPromptById(promptId);

        AiPromptKey key = AiPromptMapper.toKey(prompt);

        deactivateOtherActivePrompts(prompt);

        AiPrompt activatedPrompt = prompt.active()
                ? prompt
                : repository.save(
                prompt.activate(
                        SYSTEM_USER_ID,
                        SYSTEM_USERNAME
                )
        );

        publisher.publish(key);

        return AiPromptMapper.toDetailsResponse(activatedPrompt);
    }

    @Override
    public AiPromptDetailsResponse getPrompt(String promptId) {

        AiPrompt currentPrompt = getPromptById(promptId);

        return AiPromptMapper.toDetailsResponse(currentPrompt);
    }

    @Override
    public AiPromptDetailsResponse getActivePrompt(AiPromptKey aiPromptKey) {
        List<AiPrompt> activePrompts = findActivePrompts(aiPromptKey);

        if (activePrompts.isEmpty()) {
            throw new AiActivePromptNotFoundException(aiPromptKey);
        }

        if (activePrompts.size() > 1) {
            throw new AiPromptStateInvalidException(aiPromptKey);
        }

        return AiPromptMapper.toDetailsResponse(activePrompts.get(0));
    }

    @Override
    public List<AiPromptResponse> getPromptVersions(AiPromptKey aiPromptKey) {

        return findPromptVersions(aiPromptKey).stream()
                .map(AiPromptMapper::toResponse)
                .toList();
    }

    private Optional<AiPrompt> findLatestPrompt(AiPromptKey key) {
        return repository.findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                key.feature(),
                key.type(),
                key.targetModel()
        );
    }

    private List<AiPrompt> findPromptVersions(AiPromptKey key) {
        return repository.findByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                key.feature(),
                key.type(),
                key.targetModel()
        );
    }

    private List<AiPrompt> findActivePrompts(AiPromptKey key) {
        return repository.findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
                key.feature(),
                key.type(),
                key.targetModel()
        );
    }

    private AiPrompt buildPrompt(
            CreateAiPromptRequest request,
            long version
    ) {
        Instant now = Instant.now();

        return buildPrompt(request, version, now, SYSTEM_USER_ID, SYSTEM_USERNAME);
    }

    private AiPrompt buildPrompt(
            CreateAiPromptRequest request,
            long version,
            Instant now,
            String userId,
            String username
    ) {
        return AiPrompt.builder()
                .feature(request.feature())
                .type(request.type())
                .targetModel(request.targetModel())
                .version(version)
                .content(normalizer.normalizeContent(request.content()))
                .active(false)
                .createdByUserId(userId)
                .createdByUsername(username)
                .createdAt(now)
                .promptDescription(normalizer.normalizeShortText(request.promptDescription()))
                .versionComment(normalizer.normalizeShortText(request.versionComment()))
                .build();
    }

    private AiPrompt getPromptById(String promptId) {
        return repository.findById(promptId)
                .orElseThrow(() -> new AiPromptNotFoundException(promptId));
    }

    private void deactivateOtherActivePrompts(AiPrompt prompt) {
        deactivateOtherActivePrompts(prompt, SYSTEM_USER_ID, SYSTEM_USERNAME);
    }

    private void deactivateOtherActivePrompts(AiPrompt prompt, String userId, String username) {
        findActivePrompts(AiPromptMapper.toKey(prompt))
                .stream()
                .filter(activePrompt -> !Objects.equals(activePrompt.id(), prompt.id()))
                .map(activePrompt -> activePrompt.deactivate(userId, username))
                .forEach(repository::save);
    }

    private long resolveNextVersion(AiPromptKey aiPromptKey) {
        return findLatestPrompt(aiPromptKey)
                .map(AiPrompt::version)
                .filter(version -> version > 0)
                .map(version -> version + 1)
                .orElse(1L);
    }
}
