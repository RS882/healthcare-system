package com.healthcare.aiservice.common.prompt.normalizer;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class PromptTextNormalizer {

    private static final String EMPTY = "";
    private static final String BOM = "\uFEFF";

    public String normalizeContent(String value) {
        if (value == null) {
            return EMPTY;
        }

        return removeTrailingSpacesFromLines(
                normalizeLineEndings(
                        removeBom(value)
                ).strip()
        );
    }

    public String normalizeShortText(String value) {
        if (value == null || value.isBlank()) {
            return EMPTY;
        }

        return normalizeLineEndings(removeBom(value))
                .strip()
                .replaceAll("\\s+", " ");
    }

    private String removeBom(String value) {
        return value.startsWith(BOM)
                ? value.substring(1)
                : value;
    }

    private String normalizeLineEndings(String value) {
        return value
                .replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    private String removeTrailingSpacesFromLines(String value) {
        return Arrays.stream(value.split("\n", -1))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"));
    }
}