package com.healthcare.aiservice.common.message_classification.service;

import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationResponse;
import com.healthcare.aiservice.common.message_classification.prompt.MessageClassificationPromptProvider;
import com.healthcare.aiservice.common.provider.AiClient;

import com.healthcare.aiservice.common.provider.logging.LogAiUsage;
import com.healthcare.aiservice.config.constant.FeatureName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageClassificationService {

    private final AiClient aiClient;
    private final MessageClassificationPromptProvider promptProvider;

    @LogAiUsage(feature = FeatureName.MESSAGE_CLASSIFICATION)
    public MessageClassificationResponse classify(MessageClassificationRequest request) {
        return aiClient.call(
                promptProvider.systemPrompt(),
                promptProvider.userPrompt(request),
                MessageClassificationResponse.class
        );
    }
}