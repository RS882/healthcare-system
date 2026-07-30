package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.prompt.event.ActivePromptChangedEvent;
import com.healthcare.aiservice.common.prompt.mapper.AiPromptMapper;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivePromptChangedEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(AiPromptKey key) {
        eventPublisher.publishEvent(
                new ActivePromptChangedEvent(key)
        );
    }
}
