package com.healthcare.aiservice.common.prompt.service.interfaces;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.dto.AiPromptResponse;
import com.healthcare.aiservice.common.prompt.dto.CreateAiPromptRequest;

import java.util.List;

public interface AiPromptManagementService {

    AiPromptDetailsResponse createPrompt(CreateAiPromptRequest request);

    AiPromptDetailsResponse activatePrompt(String promptId);

    AiPromptDetailsResponse getPrompt(String promptId);

    AiPromptDetailsResponse getActivePrompt(AiPromptKey aiPromptKey);

    List<AiPromptResponse> getPromptVersions(AiPromptKey aiPromptKey);
}
