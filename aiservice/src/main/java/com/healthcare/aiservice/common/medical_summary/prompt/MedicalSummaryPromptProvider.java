package com.healthcare.aiservice.common.medical_summary.prompt;

import org.springframework.stereotype.Component;

@Component
public class MedicalSummaryPromptProvider {

    public String systemPrompt() {
        return """
                You are a medical assistant.
                Summarize medical notes clearly and safely.
                Do not invent facts.
                If information is missing, return an empty list.
                Return structured data only.
                """;
    }

    public String userPrompt(String note) {
        return """
                Summarize this medical note.

                Medical note:
                %s
                """.formatted(note);
    }
}
