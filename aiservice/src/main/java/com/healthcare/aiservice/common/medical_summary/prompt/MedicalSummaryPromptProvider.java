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
                
                Rules:
                
                1. Use only information explicitly present in the medical note.
                2. Never infer, guess, assume, or add information.
                3. Never invent diagnoses, medications, or recommendations.
                4. Preserve the original medical meaning.
                5. Return structured data matching the response schema.
                6. Do not include explanations, comments, markdown, or code fences.
                7. Do not return any additional text outside the structured response.
                
                Extract:
                
                - summary
                - diagnoses
                - medications
                - recommendations
                
                Definitions:
                
                summary:
                A concise summary of the medical note.
                
                diagnoses:
                Medical diagnoses explicitly stated in the note.
                
                medications:
                Medications or treatments explicitly mentioned in the note.
                
                recommendations:
                Advice, treatment plans, follow-up instructions, or recommendations explicitly mentioned in the note.
                
                Missing information rules:
                
                - If diagnoses are absent, return an empty collection.
                - If medications are absent, return an empty collection.
                - If recommendations are absent, return an empty collection.
                
                Medication extraction rules:
                
                - Extract medication name when available.
                - Extract dosage when explicitly provided.
                - If dosage is missing, leave the dosage field empty.
                """;
    }

    @Override
    public String userPrompt(MedicalSummaryRequest request) {
        return """
                Summarize the following medical note and extract structured medical information.
                
                Medical note:
                
                %s
                """.formatted(request.note());
    }
}
