package com.healthcare.aiservice.common.medical_summary.service;


import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.common.medical_summary.prompt.MedicalSummaryPromptProvider;
import com.healthcare.aiservice.common.provider.AiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalSummaryServiceTest {

    @Mock
    private AiClient aiClient;

    @Mock
    private MedicalSummaryPromptProvider promptProvider;

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

        when(promptProvider.systemPrompt()).thenReturn("system prompt");
        when(promptProvider.userPrompt(request)).thenReturn("user prompt");

        when(aiClient.call(
                "system prompt",
                "user prompt",
                MedicalSummaryResponse.class
        )).thenReturn(expectedResponse);

        MedicalSummaryResponse actualResponse = medicalSummaryService.summarize(request);

        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(promptProvider).systemPrompt();
        verify(promptProvider).userPrompt(request);
        verify(aiClient).call(
                "system prompt",
                "user prompt",
                MedicalSummaryResponse.class
        );

        verifyNoMoreInteractions(promptProvider, aiClient);
    }
}