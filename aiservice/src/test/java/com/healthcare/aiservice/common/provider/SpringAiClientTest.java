package com.healthcare.aiservice.common.provider;

import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.common.provider.logging.AiParsingErrorLogger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Spring AI client tests: ")
class SpringAiClientTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private AiParsingErrorLogger parsingErrorLogger;

    @InjectMocks
    private SpringAiClient springAiClient;

    @Test
    void call_ShouldReturnResponse_WhenAiProviderReturnsValidEntity() {
        String systemPrompt = "system prompt";
        String userPrompt = "user prompt";

        MedicalSummaryResponse expectedResponse = new MedicalSummaryResponse(
                "Headache reported",
                List.of(),
                List.of(),
                List.of("MRI examination")
        );

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(systemPrompt)).thenReturn(requestSpec);
        when(requestSpec.user(userPrompt)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(MedicalSummaryResponse.class))
                .thenReturn(expectedResponse);

        MedicalSummaryResponse actualResponse = springAiClient.call(
                systemPrompt,
                userPrompt,
                MedicalSummaryResponse.class
        );

        assertThat(actualResponse).isEqualTo(expectedResponse);

        verify(chatClient).prompt();
        verify(requestSpec).system(systemPrompt);
        verify(requestSpec).user(userPrompt);
        verify(requestSpec).call();
        verify(callResponseSpec).entity(MedicalSummaryResponse.class);

        verifyNoMoreInteractions(chatClient, requestSpec, callResponseSpec, parsingErrorLogger);
    }

    @Test
    void call_ShouldThrowException_WhenAiProviderFails() {
        String systemPrompt = "system prompt";
        String userPrompt = "user prompt";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(systemPrompt)).thenReturn(requestSpec);
        when(requestSpec.user(userPrompt)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(MedicalSummaryResponse.class))
                .thenThrow(new NonTransientAiException("AI provider failed"));

        assertThatThrownBy(() -> springAiClient.call(
                systemPrompt,
                userPrompt,
                MedicalSummaryResponse.class
        ))
                .isInstanceOf(NonTransientAiException.class)
                .hasMessageContaining("AI provider failed");

        verify(chatClient).prompt();
        verify(requestSpec).system(systemPrompt);
        verify(requestSpec).user(userPrompt);
        verify(requestSpec).call();
        verify(callResponseSpec).entity(MedicalSummaryResponse.class);
        verify(parsingErrorLogger).logIfParsingError(
                any(RuntimeException.class),
                eq(MedicalSummaryResponse.class)
        );

        verifyNoMoreInteractions(chatClient, requestSpec, callResponseSpec, parsingErrorLogger);
    }
}