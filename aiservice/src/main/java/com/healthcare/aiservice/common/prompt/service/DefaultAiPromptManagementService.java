package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.dto.AiPromptResponse;
import com.healthcare.aiservice.common.prompt.dto.CreateAiPromptRequest;
import com.healthcare.aiservice.common.prompt.mapper.AiPromptMapper;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptManagementService;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.repository.AiPromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultAiPromptManagementService implements AiPromptManagementService {

    private static final String SYSTEM_USER_ID = "system";
    private static final String SYSTEM_USERNAME = "system";

    private final AiPromptRepository repository;

    @Override
    public AiPromptDetailsResponse createPrompt(CreateAiPromptRequest request) {
        long nextVersion = resolveNextVersion(request.feature(), request.type(), request.targetModel());

        Instant now = Instant.now();

        AiPrompt prompt = AiPrompt.builder()
                .feature(request.feature())
                .type(request.type())
                .targetModel(request.targetModel())
                .version(nextVersion)
                .content(request.content().strip())
                .active(false)
                .createdByUserId(SYSTEM_USER_ID)
                .createdByUsername(SYSTEM_USERNAME)
                .createdAt(now)
                .updatedByUserId(SYSTEM_USER_ID)
                .updatedByUsername(SYSTEM_USERNAME)
                .updatedAt(now)
                .promptDescription(normalizeText(request.promptDescription()))
                .versionComment(normalizeText(request.versionComment()))
                .build();

        AiPrompt savedPrompt = repository.save(prompt);


        return AiPromptMapper.toDetailsResponse(savedPrompt);
    }

    @Override
    public AiPromptDetailsResponse activatePrompt(String promptId) {
        return null;
    }

    @Override
    public AiPromptDetailsResponse getPrompt(String promptId) {
        return null;
    }

    @Override
    public AiPromptDetailsResponse getActivePrompt(FeatureName feature, PromptType type, AiProviderModel targetModel) {
        return null;
    }

    @Override
    public List<AiPromptResponse> getPromptVersions(FeatureName feature, PromptType type, AiProviderModel targetModel) {
        return List.of();
    }

    private long resolveNextVersion(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    ) {
        return repository.findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
                        feature,
                        type,
                        targetModel
                )
                .map(AiPrompt::version)
                .filter(version -> version > 0)
                .map(version -> version + 1)
                .orElse(1L);
    }

    private String normalizeText(String text) {
        return StringUtils.hasText(text) ? text.strip() : "";
    }
}
