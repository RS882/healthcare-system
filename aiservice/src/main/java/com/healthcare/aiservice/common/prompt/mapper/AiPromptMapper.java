package com.healthcare.aiservice.common.prompt.mapper;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.dto.AiPromptResponse;
import com.healthcare.aiservice.common.prompt.dto.CreateAiPromptRequest;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;

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


    public static AiPromptKey toKey(CreateAiPromptRequest request) {
        return AiPromptKey.builder()
                .feature(request.feature())
                .type(request.type())
                .targetModel(request.targetModel())
                .build();
    }

    public static AiPromptKey toKey(AiPrompt prompt) {
        return AiPromptKey.builder()
                .feature(prompt.feature())
                .type(prompt.type())
                .targetModel(prompt.targetModel())
                .build();
    }
}