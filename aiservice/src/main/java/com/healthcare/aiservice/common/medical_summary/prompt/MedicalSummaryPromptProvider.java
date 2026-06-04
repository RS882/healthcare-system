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
            - Use only information explicitly present in the medical note.
            - If diagnoses are missing, return an empty array.
            - If medications are missing, return an empty array.
            - If recommendations are missing, return an empty array.
            
            Medication extraction rules:
            - Each medication must be an object.
            - Each medication object must contain:
              - name
              - dosage
            - If medication dosage is missing, use an empty string.
            - Do NOT return medications as plain strings.
            
            Expected response structure:
            {
              "summary": "string",
              "diagnoses": ["string"],
              "medications": [
                {
                  "name": "string",
                  "dosage": "string"
                }
              ],
              "recommendations": ["string"]
            }
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
