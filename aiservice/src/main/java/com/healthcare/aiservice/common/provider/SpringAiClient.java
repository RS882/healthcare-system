package com.healthcare.aiservice.common.provider;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.exception.AiResponseParsingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringAiClient implements AiClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public <T> T call(String systemPrompt, String userPrompt, Class<T> responseType) {

        String rawResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        String json = JsonExtractor.extractObject(rawResponse);

        try {
            return objectMapper.readValue(json, responseType);
        } catch (JsonProcessingException ex) {
            throw new AiResponseParsingException(
                    "Failed to parse AI response to " + responseType.getSimpleName(),
                    rawResponse,
                    json,
                    ex
            );
        }
    }
}