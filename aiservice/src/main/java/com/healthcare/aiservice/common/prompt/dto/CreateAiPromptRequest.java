package com.healthcare.aiservice.common.prompt.dto;

import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.config.constant.FeatureName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Schema(
        name = "CreateAiPromptVersionRequest",
        description = "Request for creating a new AI prompt version"
)
public record CreateAiPromptRequest(

        @Schema(
                description = "Feature name",
                example = "MEDICAL_SUMMARY"
        )
        @NotNull(message = "Feature must not be null")
        FeatureName feature,

        @Schema(
                description = "Type of prompt",
                example = "SYSTEM"
        )
        @NotNull(message = "Prompt type must not be null")
        PromptType type,

        @NotNull(message = "Target model must not be null")
        @Schema(
                description = "Target AI model",
                example = "LLAMA3"
        )
        AiProviderModel targetModel,

        @Schema(
                description = "Prompt content",
                example = """
                        You are a medical information extraction assistant.
                        
                        Extract only information explicitly stated in the medical note.
                        
                        Return only valid JSON.
                        """
        )
        @NotBlank(message = "Content must not be empty")
        @Size(
                min = 10,
                max = 30000,
                message = "Content length must be between 10 and 30000 characters"
        )
        String content,

        @Schema(
                description = "Description of prompt",
                example = "Strict JSON schema v2"
        )
        @Size(
                max = 500,
                message = "Description length must not exceed 500 characters")
        String promptDescription,

        @Size(
                max = 500,
                message = "Version comment length must not exceed 500 characters"
        )
        @Schema(
                description = "Reason for creating this prompt version",
                example = "Added medication dosage extraction"
        )
        String versionComment
) {
}