package com.healthcare.aiservice.common.prompt.mapper;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.dto.AiPromptResponse;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;

import java.util.List;

public final class AiPromptMapper {

    private AiPromptMapper() {
    }

    public static AiPromptResponse toResponse(AiPrompt prompt) {

        if (prompt == null) {
            return null;
        }

        return new AiPromptResponse(
                prompt.id(),
                prompt.feature(),
                prompt.type(),
                prompt.targetModel(),
                prompt.version(),
                prompt.active(),
                prompt.createdByUsername(),
                prompt.createdAt(),
                prompt.updatedAt()
        );
    }

    public static AiPromptDetailsResponse toDetailsResponse(AiPrompt prompt) {

        if (prompt == null) {
            return null;
        }

        return new AiPromptDetailsResponse(
                prompt.id(),
                prompt.feature(),
                prompt.type(),
                prompt.targetModel(),
                prompt.version(),
                prompt.content(),
                prompt.active(),
                prompt.promptDescription(),
                prompt.versionComment(),
                prompt.createdByUserId(),
                prompt.createdByUsername(),
                prompt.updatedByUserId(),
                prompt.updatedByUsername(),
                prompt.createdAt(),
                prompt.updatedAt()
        );
    }

    public static List<AiPromptResponse> toResponseList(
            List<AiPrompt> prompts
    ) {
        return prompts.stream()
                .map(AiPromptMapper::toResponse)
                .toList();
    }

    public static List<AiPromptDetailsResponse> toDetailsResponseList(
            List<AiPrompt> prompts
    ){
        return prompts.stream()
                .map(AiPromptMapper::toDetailsResponse)
                .toList();
    }
}