package com.healthcare.aiservice.common.prompt.model;

import com.healthcare.aiservice.config.constant.FeatureName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(
        name = "AiPromptKey",
        description = "AI prompt key metadata"
)
public record AiPromptKey(
        @Schema(
                description = "AI feature that uses this prompt",
                example = "MEDICAL_SUMMARY"
        )
        FeatureName feature,

        @Schema(
                description = "Type of prompt",
                example = "SYSTEM"
        )
        PromptType type,

        @Schema(
                description = "Target AI model for which this prompt was created",
                example = "LLAMA3"
        )
        AiProviderModel targetModel
) {
}
