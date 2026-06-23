package com.healthcare.aiservice.common.message_classification.service;

import com.healthcare.aiservice.common.message_classification.category.MessageCategory;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationResponse;
import com.healthcare.aiservice.common.message_classification.prompt.MessageClassificationPromptProvider;
import com.healthcare.aiservice.common.provider.AiClient;
import com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidExceptionMessages.CLASSIFICATION_EXCEPTION_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MessageClassificationServiceTest {

    @Mock
    private AiClient aiClient;

    @Mock
    private MessageClassificationPromptProvider promptProvider;

    @InjectMocks
    private MessageClassificationService service;

    @Test
    void classify_ShouldThrowAiResponseInvalidException_WhenResponseIsNull() {
        when(promptProvider.systemPrompt()).thenReturn("system");
        when(promptProvider.userPrompt(any())).thenReturn("user");

        when(aiClient.call(
                eq("system"),
                eq("user"),
                eq(MessageClassificationResponse.class)
        )).thenReturn(null);

        assertThatThrownBy(() -> service.classify(new MessageClassificationRequest("note")))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(CLASSIFICATION_EXCEPTION_MESSAGE);
    }

    @Test
    void classify_ShouldThrowAiResponseInvalidException_WhenCategoryIsNull() {
        when(promptProvider.systemPrompt()).thenReturn("system");
        when(promptProvider.userPrompt(any())).thenReturn("user");

        MessageClassificationResponse invalidResponse =
                new MessageClassificationResponse(null, "Valid reason");

        when(aiClient.call(
                eq("system"),
                eq("user"),
                eq(MessageClassificationResponse.class)
        )).thenReturn(invalidResponse);

        assertThatThrownBy(() -> service.classify(new MessageClassificationRequest("note")))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(CLASSIFICATION_EXCEPTION_MESSAGE);
    }

    @Test
    void classify_ShouldThrowAiResponseInvalidException_WhenReasonIsBlank() {
        when(promptProvider.systemPrompt()).thenReturn("system");
        when(promptProvider.userPrompt(any())).thenReturn("user");

        MessageClassificationResponse invalidResponse =
                new MessageClassificationResponse(
                        MessageCategory.APPOINTMENT,
                        "   "
                );

        when(aiClient.call(
                eq("system"),
                eq("user"),
                eq(MessageClassificationResponse.class)
        )).thenReturn(invalidResponse);

        assertThatThrownBy(() -> service.classify(new MessageClassificationRequest("note")))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(CLASSIFICATION_EXCEPTION_MESSAGE);
    }
}