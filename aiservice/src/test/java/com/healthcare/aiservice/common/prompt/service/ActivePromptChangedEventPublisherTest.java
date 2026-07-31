package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.event.ActivePromptChangedEvent;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Active prompt changed event publisher tests: ")
class ActivePromptChangedEventPublisherTest {

    private static final AiPromptKey PROMPT_KEY =
            new AiPromptKey(
                    FeatureName.MEDICAL_SUMMARY,
                    PromptType.SYSTEM,
                    AiProviderModel.LLAMA_3
            );

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ActivePromptChangedEventPublisher publisher;

    @Test
    void publish_ShouldPublishActivePromptChangedEvent() {
        ArgumentCaptor<ActivePromptChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(ActivePromptChangedEvent.class);

        publisher.publish(PROMPT_KEY);

        verify(applicationEventPublisher)
                .publishEvent(eventCaptor.capture());

        ActivePromptChangedEvent publishedEvent =
                eventCaptor.getValue();

        assertThat(publishedEvent.key())
                .isEqualTo(PROMPT_KEY);
    }
}