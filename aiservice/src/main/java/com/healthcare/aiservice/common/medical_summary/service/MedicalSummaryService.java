package com.healthcare.aiservice.common.medical_summary.service;

import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.common.medical_summary.prompt.MedicalSummaryPromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MedicalSummaryService {

    private final ChatClient chatClient;
    private final MedicalSummaryPromptProvider promptProvider;

    public MedicalSummaryResponse summarize(MedicalSummaryRequest request) {
        return chatClient.prompt()
                .system(promptProvider.systemPrompt())
                .user(promptProvider.userPrompt(request.note()))
                .call()
                .entity(MedicalSummaryResponse.class);
    }
}
