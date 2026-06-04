package com.healthcare.aiservice.exception;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AiExceptionInspector {

    public <T extends Throwable> Optional<T> findCause(
            Throwable throwable,
            Class<T> type
    ) {
        Throwable current = throwable;

        while (current != null) {
            if (type.isInstance(current)) {
                return Optional.of(type.cast(current));
            }

            current = current.getCause();
        }

        return Optional.empty();
    }

    public boolean containsCause(
            Throwable throwable,
            Class<? extends Throwable> type
    ) {
        return findCause(throwable, type).isPresent();
    }
}