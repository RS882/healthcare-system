package com.healthcare.aiservice.common.medical_summary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.common.medical_summary.service.MedicalSummaryService;
import com.healthcare.aiservice.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.healthcare.aiservice.common.medical_summary.controller.API.AiApiPaths.MEDICAL_NOTE_SUMMARY_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MedicalSummaryController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Medical summary controller tests: ")
class MedicalSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicalSummaryService medicalSummaryService;

    @Test
    void summarize_ShouldReturn200() throws Exception {

        MedicalSummaryRequest request =
                new MedicalSummaryRequest(
                        "Patient complains about headache."
                );

        MedicalSummaryResponse response =
                new MedicalSummaryResponse(
                        "Headache reported",
                        List.of(),
                        List.of(),
                        List.of("MRI examination")
                );

        when(medicalSummaryService.summarize(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post(MEDICAL_NOTE_SUMMARY_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary")
                        .value("Headache reported"))
                .andExpect(jsonPath("$.recommendations[0]")
                        .value("MRI examination"));
    }

    @Test
    void summarize_ShouldReturn400_WhenNoteIsBlank() throws Exception {

        MedicalSummaryRequest request =
                new MedicalSummaryRequest("");

        mockMvc.perform(
                        post(MEDICAL_NOTE_SUMMARY_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"));
    }

    @Test
    void summarize_ShouldReturn502_WhenAiProviderFails() throws Exception {

        MedicalSummaryRequest request =
                new MedicalSummaryRequest("Patient complains about headache.");

        when(medicalSummaryService.summarize(any()))
                .thenThrow(new NonTransientAiException("AI provider failed"));

        mockMvc.perform(
                        post("/v1/ai/medical-note/summary")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error")
                        .value("AI_PROVIDER_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("AI provider failed to process the request"))
                .andExpect(jsonPath("$.path")
                        .value("/v1/ai/medical-note/summary"));
    }
}