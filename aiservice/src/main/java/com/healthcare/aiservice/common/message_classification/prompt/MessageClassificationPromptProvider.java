package com.healthcare.aiservice.common.message_classification.prompt;

import com.healthcare.aiservice.common.message_classification.category.ClassificationCategoryProvider;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.prompt.PromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageClassificationPromptProvider
        implements PromptProvider<MessageClassificationRequest> {

    private final ClassificationCategoryProvider categoryProvider;

    @Override
    public String systemPrompt() {
        return """
            You are a healthcare message classification assistant.

            Your task is to classify patient messages into exactly one category.

            Allowed categories:
            %s

            Strict rules:
            - Return only valid JSON.
            - Do NOT use markdown.
            - Do NOT use code fences.
            - Do NOT add explanations outside JSON.
            - Do NOT invent facts.
            - Use only information explicitly present in the message.
            - The category must be exactly one of the allowed categories.
            - The response MUST contain both fields: category and reason.
            - The reason field MUST be a short sentence.
            - If the message mentions severe chest pain, shortness of breath, stroke symptoms, unconsciousness, heavy bleeding, or life-threatening symptoms, classify as EMERGENCY.

            Required JSON response format:
            {
              "category": "APPOINTMENT",
              "reason": "The patient wants to schedule, cancel, or reschedule an appointment."
            }
            """.formatted(categoryProvider.getAllowedCategoriesAsPromptText());
    }

    @Override
    public String userPrompt(MessageClassificationRequest request) {
        return """
            Classify the following patient message.

            Patient message:
            "%s"

            Return JSON with exactly these fields:
            {
              "category": "...",
              "reason": "..."
            }
            """.formatted(request.note());
    }
}
