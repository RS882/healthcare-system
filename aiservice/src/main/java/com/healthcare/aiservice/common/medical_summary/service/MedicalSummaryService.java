package com.healthcare.aiservice.common.medical_summary.service;

import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.common.medical_summary.prompt.MedicalSummaryPromptProvider;
import com.healthcare.aiservice.common.provider.AiClient;
import com.healthcare.aiservice.common.provider.logging.annotation.LogAiUsage;
import com.healthcare.aiservice.config.constant.FeatureName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicalSummaryService {

    private final AiClient aiClient;
    private final MedicalSummaryPromptProvider promptProvider;

    @LogAiUsage(feature = FeatureName.MEDICAL_SUMMARY)
    public MedicalSummaryResponse summarize(MedicalSummaryRequest request) {
        return aiClient.call(
                promptProvider.systemPrompt(),
                promptProvider.userPrompt(request),
                MedicalSummaryResponse.class
        );
    }
}