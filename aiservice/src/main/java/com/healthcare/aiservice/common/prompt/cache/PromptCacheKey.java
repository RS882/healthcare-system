package com.healthcare.aiservice.common.prompt.cache;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;

import java.util.Locale;

public final class PromptCacheKey {

    private static final String DEFAULT_TARGET_MODEL = "default";

    private PromptCacheKey() {
    }

    public static String of(AiPromptKey key) {
        return "%s:%s:%s".formatted(
                key.feature().name(),
                key.type().name(),
                normalizeTargetModel(key.targetModel().name())
        );
    }

    private static String normalizeTargetModel(String targetModel) {
        if (targetModel == null || targetModel.isBlank()) {
            return DEFAULT_TARGET_MODEL;
        }

        return targetModel
                .strip()
                .toLowerCase(Locale.ROOT);
    }
}