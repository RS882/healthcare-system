package com.healthcare.aiservice.common.prompt.controller;

import com.healthcare.aiservice.common.prompt.controller.API.AiPromptManagementAPI;
import com.healthcare.aiservice.common.prompt.dto.*;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptManagementService;
import com.healthcare.aiservice.config.constant.FeatureName;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class AiPromptAdminController implements AiPromptManagementAPI {

    private final AiPromptManagementService service;

    @Override
    public ResponseEntity<AiPromptDetailsResponse> createPrompt(
            CreateAiPromptRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createPrompt(request));
    }

    @Override
    public ResponseEntity<AiPromptDetailsResponse> activatePrompt(
            String promptId
    ) {
        return ResponseEntity.ok(
                service.activatePrompt(promptId)
        );
    }

    @Override
    public ResponseEntity<AiPromptDetailsResponse> getPrompt(
            String promptId
    ) {
        return ResponseEntity.ok(
                service.getPrompt(promptId)
        );
    }

    @Override
    public ResponseEntity<List<AiPromptResponse>> getPromptVersions(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    ) {
        return ResponseEntity.ok(
                service.getPromptVersions(buildPromptKey(feature, type, targetModel)));

    }

    @Override
    public ResponseEntity<AiPromptDetailsResponse> getCurrentPrompt(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    ) {
        return ResponseEntity.ok(
                service.getActivePrompt(buildPromptKey(feature, type, targetModel)));
    }

    private AiPromptKey buildPromptKey(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    ) {
        return new AiPromptKey(feature, type, targetModel);
    }
}