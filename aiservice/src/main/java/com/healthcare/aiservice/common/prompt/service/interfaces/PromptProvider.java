package com.healthcare.aiservice.common.prompt.service.interfaces;

import com.healthcare.aiservice.common.dto.NoteBasedRequest;
import com.healthcare.aiservice.config.constant.FeatureName;

public interface PromptProvider<T extends NoteBasedRequest> {

    FeatureName feature();

    Class<T> requestType();

    String systemPrompt();

    String userPrompt(T input);
}
