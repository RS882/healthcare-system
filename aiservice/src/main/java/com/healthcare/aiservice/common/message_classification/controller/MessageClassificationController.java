package com.healthcare.aiservice.common.message_classification.controller;

import com.healthcare.aiservice.common.message_classification.controller.API.MessageClassificationAPI;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationResponse;
import com.healthcare.aiservice.common.message_classification.service.MessageClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor

public class MessageClassificationController implements MessageClassificationAPI {

    private final MessageClassificationService messageClassificationService;


    @Override
    public ResponseEntity<MessageClassificationResponse> classify(MessageClassificationRequest request) {
        return ResponseEntity.ok(messageClassificationService.classify(request));
    }
}
