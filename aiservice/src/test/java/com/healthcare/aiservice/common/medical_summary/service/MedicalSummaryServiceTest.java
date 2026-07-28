package com.healthcare.aiservice.common.medical_summary.service;

import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.common.medical_summary.dto.MedicationInfo;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptFactory;
import com.healthcare.aiservice.common.provider.AiClient;
import com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static com.healthcare.aiservice.config.constant.FeatureName.MEDICAL_SUMMARY;
import static com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidExceptionMessages.MEDICAL_SUMMARY_EXCEPTION_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MedicalSummaryServiceTest {

    private static final String SYSTEM_PROMPT = "system prompt";
    private static final String USER_PROMPT = "user prompt";

    @Mock
    private AiClient aiClient;

    @Mock
    private AiPromptFactory promptFactory;

    @InjectMocks
    private MedicalSummaryService medicalSummaryService;

    @Test
    void summarize_ShouldReturnMedicalSummaryResponse() {
        MedicalSummaryRequest request = new MedicalSummaryRequest(
                "Patient complains about headache. Doctor recommends MRI examination."
        );

        MedicalSummaryResponse expectedResponse = new MedicalSummaryResponse(
                "Headache reported, MRI recommended",
                List.of(),
                List.of(),
                List.of("MRI examination")
        );

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MedicalSummaryResponse.class
        )).thenReturn(expectedResponse);

        MedicalSummaryResponse actualResponse =
                medicalSummaryService.summarize(request);

        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(promptFactory)
                .getSystemPrompt(MEDICAL_SUMMARY);

        verify(promptFactory)
                .getUserPrompt(MEDICAL_SUMMARY, request);

        verify(aiClient).call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MedicalSummaryResponse.class
        );

        verifyNoMoreInteractions(promptFactory, aiClient);
    }

    @Test
    void summarize_ShouldThrowAiResponseInvalidException_WhenResponseIsNull() {
        MedicalSummaryRequest request =
                new MedicalSummaryRequest("note");

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MedicalSummaryResponse.class
        )).thenReturn(null);

        assertThatThrownBy(() -> medicalSummaryService.summarize(request))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(MEDICAL_SUMMARY_EXCEPTION_MESSAGE);
    }

    @Test
    void summarize_ShouldThrowAiResponseInvalidException_WhenSummaryIsBlank() {
        MedicalSummaryRequest request =
                new MedicalSummaryRequest("note");

        MedicalSummaryResponse invalidResponse =
                new MedicalSummaryResponse(
                        "   ",
                        List.of(),
                        List.of(),
                        List.of()
                );

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MedicalSummaryResponse.class
        )).thenReturn(invalidResponse);

        assertThatThrownBy(() -> medicalSummaryService.summarize(request))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(MEDICAL_SUMMARY_EXCEPTION_MESSAGE);
    }

    @Test
    void summarize_ShouldNormalizeResponse_WhenAiResponseContainsNullsAndBlankValues() {
        MedicalSummaryRequest request =
                new MedicalSummaryRequest("note");

        MedicalSummaryResponse aiResponse =
                new MedicalSummaryResponse(
                        "  Patient has headache.  ",
                        null,
                        Arrays.asList(
                                null,
                                new MedicationInfo(null, "500mg"),
                                new MedicationInfo("   ", "500mg"),
                                new MedicationInfo("  Ibuprofen  ", null),
                                new MedicationInfo(
                                        "  Paracetamol  ",
                                        "  500mg  "
                                )
                        ),
                        null
                );

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MedicalSummaryResponse.class
        )).thenReturn(aiResponse);

        MedicalSummaryResponse result =
                medicalSummaryService.summarize(request);

        assertThat(result.summary())
                .isEqualTo("Patient has headache.");

        assertThat(result.diagnoses())
                .isEmpty();

        assertThat(result.recommendations())
                .isEmpty();

        assertThat(result.medications()).containsExactly(
                new MedicationInfo("Ibuprofen", ""),
                new MedicationInfo("Paracetamol", "500mg")
        );
    }

    private void mockPrompts(MedicalSummaryRequest request) {
        when(promptFactory.getSystemPrompt(MEDICAL_SUMMARY))
                .thenReturn(SYSTEM_PROMPT);

        when(promptFactory.getUserPrompt(
                MEDICAL_SUMMARY,
                request
        )).thenReturn(USER_PROMPT);
    }
}