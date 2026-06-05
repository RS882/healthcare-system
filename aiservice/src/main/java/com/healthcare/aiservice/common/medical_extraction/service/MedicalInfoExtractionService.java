package com.healthcare.aiservice.common.medical_extraction.service;

import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionRequest;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionResponse;
import com.healthcare.aiservice.common.medical_extraction.prompt.MedicalInfoExtractionPromptProvider;
import com.healthcare.aiservice.common.provider.AiClient;
import com.healthcare.aiservice.common.provider.logging.LogAiUsage;
import com.healthcare.aiservice.config.constant.FeatureName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicalInfoExtractionService {

    private final AiClient aiClient;
    private final MedicalInfoExtractionPromptProvider promptProvider;

    @LogAiUsage(feature = FeatureName.MEDICAL_EXTRACTION)
    public MedicalInfoExtractionResponse extract(MedicalInfoExtractionRequest request) {

        return aiClient.call(
                promptProvider.systemPrompt(),
                promptProvider.userPrompt(request),
                MedicalInfoExtractionResponse.class
        );
    }
}
