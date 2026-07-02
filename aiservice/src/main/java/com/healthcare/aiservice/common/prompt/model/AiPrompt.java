package com.healthcare.aiservice.common.prompt.model;

import com.healthcare.aiservice.config.constant.FeatureName;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Builder
@Document(collection = "ai_prompts")
@CompoundIndex(
        name = "feature_type_model_version_idx",
        def = "{'feature':1,'type':1,'targetModel':1,'version':1}",
        unique = true
)
@CompoundIndex(
        name = "feature_type_model_active_idx",
        def = "{'feature':1,'type':1,'targetModel':1,'active':1}"
)
public record AiPrompt(

        @Id
        String id,

        FeatureName feature,

        PromptType type,

        AiProviderModel targetModel,

        @Positive(message = "Version must be greater than zero")
        long version,

        String content,

        boolean active,

        String createdByUserId,

        String createdByUsername,

        Instant createdAt,

        String updatedByUserId,

        String updatedByUsername,

        Instant updatedAt,

        String promptDescription,

        String versionComment
) {
}