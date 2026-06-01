package com.healthcare.aiservice.common.provider;


public interface AiClient {

    <T> T call(String systemPrompt, String userPrompt, Class<T> responseType);
}
