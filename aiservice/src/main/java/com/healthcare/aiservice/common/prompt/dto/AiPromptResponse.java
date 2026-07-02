package com.healthcare.aiservice.common.prompt.dto;

import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.config.constant.FeatureName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(
        name = "AiPromptResponse",
        description = "AI prompt version metadata"
)
public record AiPromptResponse(

        @Schema(
                description = "Prompt identifier",
                example = "6a462f4da54bd47af37800eb"
        )
        String id,

        @Schema(
                description = "AI feature",
                example = "MEDICAL_SUMMARY"
        )
        FeatureName feature,

        @Schema(
                description = "Prompt type",
                example = "SYSTEM"
        )
        PromptType type,

        @Schema(
                description = "Target AI model",
                example = "LLAMA3"
        )
        AiProviderModel targetModel,

        @Schema(
                description = "Prompt version",
                example = "4"
        )
        long version,

        @Schema(
                description = "Whether this prompt version is active",
                example = "true"
        )
        boolean active,

        @Schema(
                description = "Prompt creator username",
                example = "admin"
        )
        String createdByUsername,

        @Schema(
                description = "Prompt creation timestamp",
                example = "2026-07-02T10:15:30Z"
        )
        Instant createdAt,

        @Schema(
                description = "Last update timestamp",
                example = "2026-07-03T14:20:10Z"
        )
        Instant updatedAt
) {
}