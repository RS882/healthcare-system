package com.healthcare.aiservice.common.prompt.event;

import com.healthcare.aiservice.common.prompt.service.PromptCacheEvictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ActivePromptChangedEventListener {

    private final PromptCacheEvictionService cacheEvictionService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(ActivePromptChangedEvent event) {
        cacheEvictionService.evictIfPresent(event.key());
    }
}
