package com.healthcare.aiservice.common.prompt.dto;

import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.config.constant.FeatureName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Schema(
        name = "AiPromptDetailsResponse",
        description = "Detailed information about an AI prompt version"
)
@Builder
public record AiPromptDetailsResponse(

        @Schema(
                description = "Unique identifier of the prompt version",
                example = "6a462f4da54bd47af37800eb"
        )
        String id,

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
        AiProviderModel targetModel,

        @Schema(
                description = "Sequential version number of the prompt",
                example = "4"
        )
        long version,

        @Schema(
                description = "Complete prompt content sent to the AI model"
        )
        String content,

        @Schema(
                description = "Indicates whether this is the currently active prompt version",
                example = "true"
        )
        boolean active,

        @Schema(
                description = "Short description of the prompt purpose",
                example = "Medical Summary System Prompt"
        )
        String description,

        @Schema(
                description = "Comment describing changes introduced in this version",
                example = "Added medication dosage extraction"
        )
        String versionComment,

        @Schema(
                description = "Identifier of the user who created this prompt version",
                example = "9f12d44c"
        )
        String createdByUserId,

        @Schema(
                description = "Username of the user who created this prompt version",
                example = "admin"
        )
        String createdByUsername,

        @Schema(
                description = "Identifier of the user who last updated this prompt version",
                example = "9f12d44c"
        )
        String updatedByUserId,

        @Schema(
                description = "Username of the user who last updated this prompt version",
                example = "admin"
        )
        String updatedByUsername,

        @Schema(
                description = "Timestamp when the prompt version was created",
                example = "2026-07-02T10:15:30Z"
        )
        Instant createdAt,

        @Schema(
                description = "Timestamp of the most recent update",
                example = "2026-07-03T14:20:10Z"
        )
        Instant updatedAt
) {
}