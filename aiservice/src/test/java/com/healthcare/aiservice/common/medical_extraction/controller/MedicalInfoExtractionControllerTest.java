package com.healthcare.aiservice.common.medical_extraction.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionRequest;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionResponse;
import com.healthcare.aiservice.common.medical_extraction.service.MedicalInfoExtractionService;
import com.healthcare.aiservice.security.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.healthcare.aiservice.common.medical_extraction.controller.API.MedicalInfoExtractionApiPaths.EXTRACT_MEDICAL_INFO_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicalInfoExtractionController.class)
@Import(SecurityConfig.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Medical info extraction controller tests: ")
class MedicalInfoExtractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicalInfoExtractionService medicalInfoExtractionService;

    @MockitoBean
    private ChatClient.Builder chatClientBuilder;

    @Test
    @DisplayName("Should extract medical info and return 200")
    void shouldExtractMedicalInfoAndReturnOk() throws Exception {
        MedicalInfoExtractionRequest request = new MedicalInfoExtractionRequest(
                "Patient reports fever and dry cough. Allergic to Penicillin."
        );

        MedicalInfoExtractionResponse response = new MedicalInfoExtractionResponse(
                List.of("Fever", "Dry cough"),
                List.of(),
                List.of(),
                List.of("Penicillin"),
                List.of(),
                List.of()
        );

        when(medicalInfoExtractionService.extract(any(MedicalInfoExtractionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post(EXTRACT_MEDICAL_INFO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symptoms[0]").value("Fever"))
                .andExpect(jsonPath("$.symptoms[1]").value("Dry cough"))
                .andExpect(jsonPath("$.allergies[0]").value("Penicillin"));

        verify(medicalInfoExtractionService).extract(any(MedicalInfoExtractionRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when note is blank")
    void shouldReturnBadRequestWhenNoteIsBlank() throws Exception {
        MedicalInfoExtractionRequest request = new MedicalInfoExtractionRequest("");

        mockMvc.perform(post(EXTRACT_MEDICAL_INFO_URL)
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

        mockMvc.perform(post(EXTRACT_MEDICAL_INFO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}