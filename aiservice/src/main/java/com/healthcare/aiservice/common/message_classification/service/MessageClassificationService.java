package com.healthcare.aiservice.common.message_classification.service;

import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationResponse;
import com.healthcare.aiservice.common.message_classification.prompt.MessageClassificationPromptProvider;
import com.healthcare.aiservice.common.provider.AiClient;
import com.healthcare.aiservice.common.provider.logging.annotation.LogAiUsage;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidExceptionMessages.CLASSIFICATION_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class MessageClassificationService {

    private final AiClient aiClient;
    private final MessageClassificationPromptProvider promptProvider;

    @LogAiUsage(feature = FeatureName.MESSAGE_CLASSIFICATION)
    public MessageClassificationResponse classify(MessageClassificationRequest request) {

        MessageClassificationResponse response = aiClient.call(
                promptProvider.systemPrompt(),
                promptProvider.userPrompt(request),
                MessageClassificationResponse.class
        );

        if (response == null
                || response.category() == null
                || !StringUtils.hasText(response.reason())) {
            throw new AiResponseInvalidException(
                    CLASSIFICATION_EXCEPTION_MESSAGE
            );
        }
        return normalizeMessageClassificationResponse(response);
    }

    private MessageClassificationResponse normalizeMessageClassificationResponse(MessageClassificationResponse response) {

        return new MessageClassificationResponse(
                response.category(),
                response.reason().strip()
        );
    }
}