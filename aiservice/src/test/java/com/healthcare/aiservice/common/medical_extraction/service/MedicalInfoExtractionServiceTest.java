package com.healthcare.aiservice.common.medical_extraction.service;

import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionRequest;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionResponse;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptFactory;
import com.healthcare.aiservice.common.provider.AiClient;
import com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static com.healthcare.aiservice.config.constant.FeatureName.MEDICAL_EXTRACTION;
import static com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidExceptionMessages.MEDICAL_INFORMATION_EXTRACTION_EXCEPTION_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalInfoExtractionServiceTest {

    private static final String SYSTEM_PROMPT = "system";
    private static final String USER_PROMPT = "user";

    @Mock
    private AiClient aiClient;

    @Mock
    private AiPromptFactory promptFactory;

    @InjectMocks
    private MedicalInfoExtractionService service;

    @Test
    void extract_ShouldThrowAiResponseInvalidException_WhenResponseIsNull() {
        MedicalInfoExtractionRequest request =
                new MedicalInfoExtractionRequest("note");

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MedicalInfoExtractionResponse.class
        )).thenReturn(null);

        assertThatThrownBy(() -> service.extract(request))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(
                        MEDICAL_INFORMATION_EXTRACTION_EXCEPTION_MESSAGE
                );
    }

    @Test
    void extract_ShouldNormalizeResponse_WhenListsContainNullsAndBlankValues() {
        MedicalInfoExtractionRequest request =
                new MedicalInfoExtractionRequest(
                        "Patient has fever and headache."
                );

        MedicalInfoExtractionResponse aiResponse =
                new MedicalInfoExtractionResponse(
                        Arrays.asList(
                                null,
                                "",
                                "   ",
                                "  Fever  ",
                                "  Headache  "
                        ),
                        Arrays.asList(
                                null,
                                "  Viral infection  "
                        ),
                        null,
                        Arrays.asList(
                                "   ",
                                "  Penicillin  "
                        ),
                        Arrays.asList(
                                null,
                                "  Chest X-ray  "
                        ),
                        Arrays.asList(
                                "",
                                "  Rest  ",
                                "  Hydration  "
                        )
                );

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MedicalInfoExtractionResponse.class
        )).thenReturn(aiResponse);

        MedicalInfoExtractionResponse result = service.extract(request);

        assertThat(result.symptoms())
                .containsExactly("Fever", "Headache");

        assertThat(result.diagnoses())
                .containsExactly("Viral infection");

        assertThat(result.medications())
                .isEmpty();

        assertThat(result.allergies())
                .containsExactly("Penicillin");

        assertThat(result.procedures())
                .containsExactly("Chest X-ray");

        assertThat(result.recommendations())
                .containsExactly("Rest", "Hydration");
    }

    private void mockPrompts(MedicalInfoExtractionRequest request) {
        when(promptFactory.getSystemPrompt(MEDICAL_EXTRACTION))
                .thenReturn(SYSTEM_PROMPT);

        when(promptFactory.getUserPrompt(
                MEDICAL_EXTRACTION,
                request
        )).thenReturn(USER_PROMPT);
    }
}