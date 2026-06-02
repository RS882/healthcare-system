package com.healthcare.aiservice.common.prompt;

import com.healthcare.aiservice.common.dto.NoteBasedRequest;

public interface PromptProvider<T extends NoteBasedRequest> {

    String systemPrompt();

    String userPrompt(T input);
}
