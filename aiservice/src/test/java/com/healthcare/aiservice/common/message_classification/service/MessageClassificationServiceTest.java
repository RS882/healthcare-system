package com.healthcare.aiservice.common.message_classification.service;

import com.healthcare.aiservice.common.message_classification.category.MessageCategory;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationResponse;
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

import static com.healthcare.aiservice.config.constant.FeatureName.MESSAGE_CLASSIFICATION;
import static com.healthcare.aiservice.exception.ai_response_invalid_exception.AiResponseInvalidExceptionMessages.CLASSIFICATION_EXCEPTION_MESSAGE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MessageClassificationServiceTest {

    private static final String SYSTEM_PROMPT = "system";
    private static final String USER_PROMPT = "user";

    @Mock
    private AiClient aiClient;

    @Mock
    private AiPromptFactory promptFactory;

    @InjectMocks
    private MessageClassificationService service;

    @Test
    void classify_ShouldThrowAiResponseInvalidException_WhenResponseIsNull() {
        MessageClassificationRequest request =
                new MessageClassificationRequest("note");

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MessageClassificationResponse.class
        )).thenReturn(null);

        assertThatThrownBy(() -> service.classify(request))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(CLASSIFICATION_EXCEPTION_MESSAGE);
    }

    @Test
    void classify_ShouldThrowAiResponseInvalidException_WhenCategoryIsNull() {
        MessageClassificationRequest request =
                new MessageClassificationRequest("note");

        MessageClassificationResponse invalidResponse =
                new MessageClassificationResponse(null, "Valid reason");

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MessageClassificationResponse.class
        )).thenReturn(invalidResponse);

        assertThatThrownBy(() -> service.classify(request))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(CLASSIFICATION_EXCEPTION_MESSAGE);
    }

    @Test
    void classify_ShouldThrowAiResponseInvalidException_WhenReasonIsBlank() {
        MessageClassificationRequest request =
                new MessageClassificationRequest("note");

        MessageClassificationResponse invalidResponse =
                new MessageClassificationResponse(
                        MessageCategory.APPOINTMENT,
                        "   "
                );

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MessageClassificationResponse.class
        )).thenReturn(invalidResponse);

        assertThatThrownBy(() -> service.classify(request))
                .isInstanceOf(AiResponseInvalidException.class)
                .hasMessageContaining(CLASSIFICATION_EXCEPTION_MESSAGE);
    }

    @Test
    void classify_ShouldNormalizeResponse_WhenReasonContainsWhitespaces() {
        MessageClassificationRequest request =
                new MessageClassificationRequest(
                        "I need to reschedule my appointment."
                );

        MessageClassificationResponse aiResponse =
                new MessageClassificationResponse(
                        MessageCategory.APPOINTMENT,
                        "  Patient wants to reschedule an appointment.  "
                );

        mockPrompts(request);

        when(aiClient.call(
                SYSTEM_PROMPT,
                USER_PROMPT,
                MessageClassificationResponse.class
        )).thenReturn(aiResponse);

        MessageClassificationResponse result = service.classify(request);

        assertThat(result.category())
                .isEqualTo(MessageCategory.APPOINTMENT);

        assertThat(result.reason())
                .isEqualTo("Patient wants to reschedule an appointment.");
    }

    private void mockPrompts(MessageClassificationRequest request) {
        when(promptFactory.getSystemPrompt(MESSAGE_CLASSIFICATION))
                .thenReturn(SYSTEM_PROMPT);

        when(promptFactory.getUserPrompt(
                MESSAGE_CLASSIFICATION,
                request
        )).thenReturn(USER_PROMPT);
    }
}