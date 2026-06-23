package com.healthcare.aiservice.common.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.common.provider.logging.AiParsingErrorLogger;
import com.healthcare.aiservice.exception.AiResponseParsingException;
import com.healthcare.aiservice.exception.JsonExtractorException;
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
    private ObjectMapper objectMapper;

    @Mock
    private AiParsingErrorLogger parsingErrorLogger;

    @InjectMocks
    private SpringAiClient springAiClient;

    @Test
    void call_ShouldReturnResponse_WhenAiProviderReturnsValidJson() throws Exception {
        String systemPrompt = "system prompt";
        String userPrompt = "user prompt";

        String rawResponse = """
                {
                  "summary": "Headache reported",
                  "diagnoses": [],
                  "medications": [],
                  "recommendations": ["MRI examination"]
                }
                """;

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
        when(callResponseSpec.content()).thenReturn(rawResponse);
        when(objectMapper.readValue(any(String.class), eq(MedicalSummaryResponse.class)))
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
        verify(callResponseSpec).content();
        verify(objectMapper).readValue(any(String.class), eq(MedicalSummaryResponse.class));

        verifyNoMoreInteractions(chatClient, requestSpec, callResponseSpec, objectMapper, parsingErrorLogger);
    }

    @Test
    void call_ShouldThrowException_WhenAiProviderFails() {
        String systemPrompt = "system prompt";
        String userPrompt = "user prompt";

        NonTransientAiException exception = new NonTransientAiException("AI provider failed");

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(systemPrompt)).thenReturn(requestSpec);
        when(requestSpec.user(userPrompt)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenThrow(exception);

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
        verify(callResponseSpec).content();

        verifyNoMoreInteractions(chatClient, requestSpec, callResponseSpec, objectMapper, parsingErrorLogger);
    }

    @Test
    void call_ShouldThrowJsonExtractorException_WhenAiResponseIsEmpty() {
        String systemPrompt = "system prompt";
        String userPrompt = "user prompt";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(systemPrompt)).thenReturn(requestSpec);
        when(requestSpec.user(userPrompt)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("   ");

        assertThatThrownBy(() -> springAiClient.call(
                systemPrompt,
                userPrompt,
                MedicalSummaryResponse.class
        ))
                .isInstanceOf(JsonExtractorException.class)
                .hasMessageContaining("AI response is empty");

        verify(chatClient).prompt();
        verify(requestSpec).system(systemPrompt);
        verify(requestSpec).user(userPrompt);
        verify(requestSpec).call();
        verify(callResponseSpec).content();

        verifyNoMoreInteractions(chatClient, requestSpec, callResponseSpec, objectMapper, parsingErrorLogger);
    }

    @Test
    void call_ShouldThrowJsonExtractorException_WhenJsonObjectNotFound() {
        String systemPrompt = "system prompt";
        String userPrompt = "user prompt";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(systemPrompt)).thenReturn(requestSpec);
        when(requestSpec.user(userPrompt)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("""
            Here is the result:

            Symptoms:
            - Fever
            - Headache
            """);

        assertThatThrownBy(() -> springAiClient.call(
                systemPrompt,
                userPrompt,
                MedicalSummaryResponse.class
        ))
                .isInstanceOf(JsonExtractorException.class)
                .hasMessageContaining("JSON object not found in AI response");

        verify(chatClient).prompt();
        verify(requestSpec).system(systemPrompt);
        verify(requestSpec).user(userPrompt);
        verify(requestSpec).call();
        verify(callResponseSpec).content();

        verifyNoMoreInteractions(chatClient, requestSpec, callResponseSpec, objectMapper, parsingErrorLogger);
    }

    @Test
    void call_ShouldThrowAiResponseParsingException_WhenJsonCannotBeParsed() throws Exception {
        String systemPrompt = "system prompt";
        String userPrompt = "user prompt";

        String rawResponse = """
            {
              "summary": "Headache reported",
              "recommendations": [
                {
                  "text": "MRI examination"
                }
              ]
            }
            """;

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(systemPrompt)).thenReturn(requestSpec);
        when(requestSpec.user(userPrompt)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(rawResponse);

        when(objectMapper.readValue(any(String.class), eq(MedicalSummaryResponse.class)))
                .thenThrow(new JsonProcessingException("Cannot parse JSON") {});

        assertThatThrownBy(() -> springAiClient.call(
                systemPrompt,
                userPrompt,
                MedicalSummaryResponse.class
        ))
                .isInstanceOf(AiResponseParsingException.class)
                .hasMessageContaining("Failed to parse AI response to MedicalSummaryResponse");

        verify(chatClient).prompt();
        verify(requestSpec).system(systemPrompt);
        verify(requestSpec).user(userPrompt);
        verify(requestSpec).call();
        verify(callResponseSpec).content();
        verify(objectMapper).readValue(any(String.class), eq(MedicalSummaryResponse.class));

        verifyNoMoreInteractions(chatClient, requestSpec, callResponseSpec, objectMapper, parsingErrorLogger);
    }
}