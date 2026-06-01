package com.healthcare.aiservice.common.medical_summary.prompt;

import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.prompt.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class MedicalSummaryPromptProvider implements PromptProvider<MedicalSummaryRequest> {

    @Override
    public String systemPrompt() {
        return """
                You are a medical assistant.

                Your task is to summarize medical notes and extract structured medical information.

                Strict rules:
                - Return only structured data compatible with the expected response object.
                - Do NOT use markdown.
                - Do NOT use code fences.
                - Do NOT add explanations.
                - Do NOT invent facts.
                - If information is missing, return an empty array.
                - Use only information explicitly present in the medical note.
                """;
    }

    @Override
    public String userPrompt(MedicalSummaryRequest request) {
        return """
                Summarize the following medical note.

                Medical note:
                %s
                """.formatted(request.note());
    }
}
