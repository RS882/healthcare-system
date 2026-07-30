package com.healthcare.aiservice.common.prompt.cache;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Objects;

public final class PromptCacheKey {

    private static final String DEFAULT_TARGET_MODEL = "default";
    private static final String DELIMITER = ":";

    private PromptCacheKey() {
    }

    public static String of(AiPromptKey key) {
        Objects.requireNonNull(key, "Prompt key must not be null");
        return String.join(
                DELIMITER,
                key.feature().name(),
                key.type().name(),
                normalizeTargetModel(key.targetModel().name()));
    }

    private static String normalizeTargetModel(String targetModel) {

        return StringUtils.hasText(targetModel) ?
                targetModel.strip().toLowerCase(Locale.ROOT) :
                DEFAULT_TARGET_MODEL;
    }
}