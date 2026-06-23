package com.healthcare.aiservice.common.medical_extraction.service;

import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionRequest;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionResponse;
import com.healthcare.aiservice.common.medical_extraction.prompt.MedicalInfoExtractionPromptProvider;
import com.healthcare.aiservice.common.provider.AiClient;
import com.healthcare.aiservice.common.provider.logging.annotation.LogAiUsage;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidExceptionMessages.MEDICAL_INFORMATION_EXTRACTION_EXCEPTION_MESSAGE;

@Service
@RequiredArgsConstructor
public class MedicalInfoExtractionService {

    private final AiClient aiClient;
    private final MedicalInfoExtractionPromptProvider promptProvider;

    @LogAiUsage(feature = FeatureName.MEDICAL_EXTRACTION)
    public MedicalInfoExtractionResponse extract(MedicalInfoExtractionRequest request) {

        MedicalInfoExtractionResponse response = aiClient.call(
                promptProvider.systemPrompt(),
                promptProvider.userPrompt(request),
                MedicalInfoExtractionResponse.class
        );

        if (response == null) {
            throw new AiResponseInvalidException(
                    MEDICAL_INFORMATION_EXTRACTION_EXCEPTION_MESSAGE
            );
        }

        return normalizeMedicalInfoExtractionResponse(response);
    }

    private MedicalInfoExtractionResponse normalizeMedicalInfoExtractionResponse(
            MedicalInfoExtractionResponse response
    ) {
        return new MedicalInfoExtractionResponse(
                normalizeTextList(response.symptoms()),
                normalizeTextList(response.diagnoses()),
                normalizeTextList(response.medications()),
                normalizeTextList(response.allergies()),
                normalizeTextList(response.procedures()),
                normalizeTextList(response.recommendations())
        );
    }

    private List<String> normalizeTextList(List<String> values) {
        return values == null
                ? List.of()
                : values.stream()
                .filter(StringUtils::hasText)
                .map(String::strip)
                .toList();
    }
}
