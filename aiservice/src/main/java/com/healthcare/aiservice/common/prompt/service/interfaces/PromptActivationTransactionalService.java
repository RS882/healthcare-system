package com.healthcare.aiservice.common.prompt.service.interfaces;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;

public interface PromptActivationTransactionalService {

    AiPromptDetailsResponse activatePrompt(
            String promptId
    );
}
