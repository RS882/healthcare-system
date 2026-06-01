package com.healthcare.aiservice.common.prompt;

public interface PromptProvider<T> {

    String systemPrompt();

    String userPrompt(T input);
}
