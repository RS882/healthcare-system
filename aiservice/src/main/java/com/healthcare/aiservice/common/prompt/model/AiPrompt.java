package com.healthcare.aiservice.common.prompt.model;

import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptType;
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
    public AiPrompt activate(String userId, String username) {
        return withActive(true, userId, username);
    }

    public AiPrompt deactivate(String userId, String username) {
        return withActive(false, userId, username);
    }

    private AiPrompt withActive(
            boolean active,
            String userId,
            String username
    ) {
        Instant now = Instant.now();
        return AiPrompt.builder()
                .id(id)
                .feature(feature)
                .type(type)
                .targetModel(targetModel)
                .version(version)
                .content(content)
                .active(active)
                .createdByUserId(createdByUserId)
                .createdByUsername(createdByUsername)
                .createdAt(createdAt)
                .updatedByUserId(userId)
                .updatedByUsername(username)
                .updatedAt(now)
                .promptDescription(promptDescription)
                .versionComment(versionComment)
                .build();
    }
}