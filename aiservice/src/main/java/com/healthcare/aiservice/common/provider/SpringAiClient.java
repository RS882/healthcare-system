package com.healthcare.aiservice.common.provider;

import com.healthcare.aiservice.common.provider.logging.AiParsingErrorLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import static com.healthcare.aiservice.common.prompt.PromptTextNormalizer.normalize;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiClient implements AiClient {

    private final ChatClient chatClient;
    private final AiParsingErrorLogger parsingErrorLogger;

    @Override
    public <T> T call(
            String systemPrompt,
            String userPrompt,
            Class<T> responseType
    ) {
        try {
            return chatClient.prompt()
                    .system(normalize(systemPrompt))
                    .user(normalize(userPrompt))
                    .call()
                    .entity(responseType);

        } catch (RuntimeException ex) {
            parsingErrorLogger.logIfParsingError(ex, responseType);
            throw ex;
        }
    }
}