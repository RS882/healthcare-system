package com.healthcare.aiservice.common.prompt;

public final class PromptTextNormalizer {

    private PromptTextNormalizer() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace('\u00A0', ' ')
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
