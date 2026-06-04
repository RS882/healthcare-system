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
                - Return only structured data compatible with the expected response object.
                - Do NOT use markdown.
                - Do NOT use code fences.
                - Do NOT add explanations outside the response object.
                - Do NOT invent facts.
                - Use only information explicitly present in the message.
                - The category must be exactly one of the allowed categories.
                - If the message mentions severe chest pain, shortness of breath, stroke symptoms, unconsciousness, heavy bleeding, or life-threatening symptoms, classify as EMERGENCY.
                """.formatted(categoryProvider.getAllowedCategoriesAsPromptText());
    }

    @Override
    public String userPrompt(MessageClassificationRequest request) {
        return """
                Classify the following patient message.

                Patient message:
                %s
                """.formatted(request.note());
    }
}
