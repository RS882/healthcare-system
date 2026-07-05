package com.healthcare.aiservice.common.message_classification.service;

import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationResponse;
import com.healthcare.aiservice.common.message_classification.prompt.MessageClassificationPromptProvider;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptFactory;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptResolver;
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
    private final AiPromptFactory promptFactory;

    @LogAiUsage(feature = FeatureName.MESSAGE_CLASSIFICATION)
    public MessageClassificationResponse classify(MessageClassificationRequest request) {

        MessageClassificationResponse response = aiClient.call(
                promptFactory.getSystemPrompt(FeatureName.MESSAGE_CLASSIFICATION),
                promptFactory.getUserPrompt(FeatureName.MESSAGE_CLASSIFICATION, request),
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