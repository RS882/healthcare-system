package com.healthcare.aiservice.common.medical_summary.service;

import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.common.medical_summary.dto.MedicationInfo;
import com.healthcare.aiservice.common.medical_summary.prompt.MedicalSummaryPromptProvider;
import com.healthcare.aiservice.common.provider.AiClient;
import com.healthcare.aiservice.common.provider.logging.annotation.LogAiUsage;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.exception.AiResponseInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MedicalSummaryService {

    private final AiClient aiClient;
    private final MedicalSummaryPromptProvider promptProvider;

    @LogAiUsage(feature = FeatureName.MEDICAL_SUMMARY)
    public MedicalSummaryResponse summarize(MedicalSummaryRequest request) {

        MedicalSummaryResponse response = aiClient.call(
                promptProvider.systemPrompt(),
                promptProvider.userPrompt(request),
                MedicalSummaryResponse.class);

        if (response == null || !StringUtils.hasText(response.summary())) {
            throw new AiResponseInvalidException(
                    "AI response does not match expected medical summary schema"
            );
        }

        return normalizeMedicalSummaryResponse(response);
    }

    private MedicalSummaryResponse normalizeMedicalSummaryResponse(MedicalSummaryResponse response) {
        return new MedicalSummaryResponse(
                response.summary().strip(),
                response.diagnoses() == null ? List.of() : response.diagnoses(),
                normalizeMedications(response.medications()),
                response.recommendations() == null ? List.of() : response.recommendations()
        );
    }

    private List<MedicationInfo> normalizeMedications(List<MedicationInfo> medications) {
        return medications == null
                ? List.of()
                : medications.stream()
                .filter(Objects::nonNull)
                .filter(m -> StringUtils.hasText(m.name()))
                .map(m -> new MedicationInfo(
                        m.name().strip(),
                        StringUtils.hasText(m.dosage())
                                ? m.dosage().strip()
                                : ""
                ))
                .toList();
    }
}