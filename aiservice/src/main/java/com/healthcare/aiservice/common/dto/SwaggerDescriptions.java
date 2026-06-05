package com.healthcare.aiservice.common.dto;

public final class SwaggerDescriptions {

    public static final String NOTE_DESCRIPTION = """
            Medical note text to analyze.

            Multi-line text is supported.

            When sending JSON requests, line breaks must be escaped using \\n
            according to the JSON specification.
            """;

    private SwaggerDescriptions() {
    }
}
