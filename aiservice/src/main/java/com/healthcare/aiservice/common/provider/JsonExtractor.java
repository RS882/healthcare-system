package com.healthcare.aiservice.common.provider;


import com.healthcare.aiservice.exception.JsonExtractorException;
import org.springframework.util.StringUtils;

public final class JsonExtractor {

    private JsonExtractor() {
    }

    public static String extractObject(String text) {
        if (!StringUtils.hasText(text)) {
            throw new JsonExtractorException("AI response is empty");
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start < 0 || end < 0 || start > end) {
            throw new JsonExtractorException("JSON object not found in AI response");
        }

        return text.substring(start, end + 1);
    }
}
