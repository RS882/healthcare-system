package com.healthcare.aiservice.common.prompt.service.interfaces;

import com.healthcare.aiservice.common.dto.NoteBasedRequest;
import com.healthcare.aiservice.config.constant.FeatureName;

public interface AiPromptFactory {

    String getSystemPrompt(FeatureName feature);

    String getUserPrompt(FeatureName feature, NoteBasedRequest request);
}
