package com.healthcare.aiservice.common.message_classification.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.common.message_classification.category.MessageCategory;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationResponse;
import com.healthcare.aiservice.common.message_classification.service.MessageClassificationService;
import com.healthcare.aiservice.security.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.healthcare.aiservice.common.message_classification.controller.API.MessageClassificationApiPaths.CLASSIFY_MESSAGE_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageClassificationController.class)
@Import(SecurityConfig.class)
class MessageClassificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageClassificationService messageClassificationService;

    @MockitoBean
    private ChatClient.Builder chatClientBuilder;

    @Test
    @DisplayName("Should classify message and return 200")
    void shouldClassifyMessageAndReturnOk() throws Exception {
        MessageClassificationRequest request = new MessageClassificationRequest(
                "I need to reschedule my appointment."
        );

        MessageClassificationResponse response = new MessageClassificationResponse(
                MessageCategory.APPOINTMENT, ""
        );

        when(messageClassificationService.classify(any(MessageClassificationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(CLASSIFY_MESSAGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value(MessageCategory.APPOINTMENT.name()));

        verify(messageClassificationService).classify(any(MessageClassificationRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when message note is blank")
    void shouldReturnBadRequestWhenNoteIsBlank() throws Exception {
        MessageClassificationRequest request = new MessageClassificationRequest("");

        mockMvc.perform(post(CLASSIFY_MESSAGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Should return 400 for malformed JSON")
    void shouldReturnBadRequestForMalformedJson() throws Exception {
        String malformedJson = """
                { "note": "Line 1
                Line 2" }
                """;

        mockMvc.perform(post(CLASSIFY_MESSAGE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}