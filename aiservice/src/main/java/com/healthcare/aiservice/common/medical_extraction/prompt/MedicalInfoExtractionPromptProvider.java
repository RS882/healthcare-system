package com.healthcare.aiservice.common.medical_extraction.prompt;

import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionRequest;
import com.healthcare.aiservice.common.prompt.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class MedicalInfoExtractionPromptProvider implements PromptProvider<MedicalInfoExtractionRequest> {
    @Override
    public String systemPrompt() {
        return """
            You are a medical information extraction assistant.

            Your task is to extract structured medical information from a medical note.

            CRITICAL OUTPUT RULES:
            - Return ONLY valid JSON.
            - Do NOT use markdown.
            - Do NOT use bullet points.
            - Do NOT use headings.
            - Do NOT use explanations.
            - Do NOT wrap the JSON in code fences.
            - The response MUST be a single JSON object.
            - All fields MUST be present.
            - All fields MUST be arrays of strings.
            - If information is absent, return an empty array for that field.

            Required JSON schema:
            {
              "symptoms": [],
              "diagnoses": [],
              "medications": [],
              "allergies": [],
              "procedures": [],
              "recommendations": []
            }

            Correct example:
            {
              "symptoms": [
                "Fever",
                "Headache",
                "Dry cough"
              ],
              "diagnoses": [
                "Viral upper respiratory infection"
              ],
              "medications": [
                "Paracetamol 500mg"
              ],
              "allergies": [],
              "procedures": [],
              "recommendations": [
                "Rest",
                "Hydration"
              ]
            }

            Incorrect output:
            Symptoms:
            - Fever
            - Headache

            Incorrect output:
            {
              "recommendations": [
                {
                  "text": "Follow-up in two weeks"
                }
              ]
            }

            Extraction rules:
            1. Extract only information explicitly stated in the note.
            2. Never infer, guess, assume, or add information.
            3. Never generate medical conclusions that are not present in the note.
            4. Preserve the original medical meaning.
            5. Do not rewrite or summarize the note.

            Field definitions:

            symptoms:
            Patient-reported symptoms, complaints, or clinical manifestations.

            diagnoses:
            Medical diagnoses explicitly stated in the note.

            medications:
            Drugs, medications, supplements, or treatments explicitly mentioned.

            allergies:
            Allergies, intolerances, or adverse reactions explicitly mentioned.

            procedures:
            Medical procedures, examinations, imaging studies, laboratory tests, surgeries, or interventions.

            recommendations:
            Advice, treatment plans, follow-up instructions, lifestyle recommendations, or physician recommendations.
            """;
    }

    @Override
    public String userPrompt(MedicalInfoExtractionRequest request) {
        return """
            Extract structured medical information from the following medical note.

            Return ONLY the JSON object. No markdown. No explanation.

            Medical note:
            "%s"
            """.formatted(request.note());
    }
}
