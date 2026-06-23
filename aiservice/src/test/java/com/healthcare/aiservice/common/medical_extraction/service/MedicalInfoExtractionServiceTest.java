package com.healthcare.aiservice.common.medical_extraction.service;

import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionRequest;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionResponse;
import com.healthcare.aiservice.common.medical_extraction.prompt.MedicalInfoExtractionPromptProvider;
import com.healthcare.aiservice.common.provider.AiClient;
import com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidExceptionMessages.MEDICAL_INFORMATION_EXTRACTION_EXCEPTION_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalInfoExtractionServiceTest {

    @Mock
    private AiClient aiClient;

    @Mock
    private MedicalInfoExtractionPromptProvider promptProvider;

    @InjectMocks
    private MedicalInfoExtractionService service;

    @Test
    void extract_ShouldThrowAiResponseInvalidException_WhenResponseIsNull() {
        when(promptProvider.systemPrompt()).thenReturn("system");
        when(promptProvider.userPrompt(any())).thenReturn("user");

        when(aiClient.call(
                eq("system"),
                eq("user"),
                eq(MedicalInfoExtractionResponse.class)
        )).thenReturn(null);

        assertThatThrownBy(() -> service.extract(new MedicalInfoExtractionRequest("note")))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(MEDICAL_INFORMATION_EXTRACTION_EXCEPTION_MESSAGE);
    }

    @Test
    void extract_ShouldNormalizeResponse_WhenListsContainNullsAndBlankValues() {
        MedicalInfoExtractionRequest request =
                new MedicalInfoExtractionRequest("Patient has fever and headache.");

        MedicalInfoExtractionResponse aiResponse =
                new MedicalInfoExtractionResponse(
                        Arrays.asList(null, "", "   ", "  Fever  ", "  Headache  "),
                        Arrays.asList(null, "  Viral infection  "),
                        null,
                        Arrays.asList("   ", "  Penicillin  "),
                        Arrays.asList(null, "  Chest X-ray  "),
                        Arrays.asList("", "  Rest  ", "  Hydration  ")
                );

        when(promptProvider.systemPrompt()).thenReturn("system");
        when(promptProvider.userPrompt(request)).thenReturn("user");

        when(aiClient.call(
                eq("system"),
                eq("user"),
                eq(MedicalInfoExtractionResponse.class)
        )).thenReturn(aiResponse);

        MedicalInfoExtractionResponse result = service.extract(request);

        assertThat(result.symptoms()).containsExactly("Fever", "Headache");
        assertThat(result.diagnoses()).containsExactly("Viral infection");
        assertThat(result.medications()).isEmpty();
        assertThat(result.allergies()).containsExactly("Penicillin");
        assertThat(result.procedures()).containsExactly("Chest X-ray");
        assertThat(result.recommendations()).containsExactly("Rest", "Hydration");
    }
}