package com.healthcare.aiservice.common.prompt.service.interfaces;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.dto.AiPromptResponse;
import com.healthcare.aiservice.common.prompt.dto.CreateAiPromptRequest;
import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.config.constant.FeatureName;

import java.util.List;

public interface AiPromptManagementService {

    AiPromptDetailsResponse createPrompt(CreateAiPromptRequest request);

    AiPromptDetailsResponse activatePrompt(String promptId);

    AiPromptDetailsResponse getPrompt(String promptId);

    AiPromptDetailsResponse getActivePrompt(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    );

    List<AiPromptResponse> getPromptVersions(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    );
}
