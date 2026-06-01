package com.healthcare.aiservice.common.provider;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringAiClient implements AiClient {

    private final ChatClient chatClient;

    @Override
    public <T> T call(String systemPrompt, String userPrompt, Class<T> responseType) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(responseType);
    }
}
