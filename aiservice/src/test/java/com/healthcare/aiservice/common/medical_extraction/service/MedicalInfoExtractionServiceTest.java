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

import static com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidExceptionMessages.MEDICAL_INFORMATION_EXTRACTION_EXCEPTION_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
}