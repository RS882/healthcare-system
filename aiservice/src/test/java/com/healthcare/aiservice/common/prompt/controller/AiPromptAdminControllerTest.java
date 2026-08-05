package com.healthcare.aiservice.common.prompt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.common.message_classification.controller.MessageClassificationController;
import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.dto.AiPromptResponse;
import com.healthcare.aiservice.common.prompt.dto.CreateAiPromptRequest;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptManagementService;
import com.healthcare.aiservice.common.web.converter.NormalizedStringToEnumConverterFactory;
import com.healthcare.aiservice.config.constant.FeatureName;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static com.healthcare.aiservice.common.prompt.controller.API.AiPromptApiPaths.ACTIVATE_PROMPT_URL;
import static com.healthcare.aiservice.common.prompt.controller.API.AiPromptApiPaths.CURRENT_PROMPT_URL;
import static com.healthcare.aiservice.common.prompt.controller.API.AiPromptApiPaths.PROMPTS_URL;
import static com.healthcare.aiservice.common.prompt.controller.API.AiPromptApiPaths.PROMPT_BY_ID_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(
        controllers = AiPromptAdminController.class,
        properties = {
                "auth-filter.enabled=false",
                "request-id-filter.enabled=false",
                "user-context-filter.enabled=false"
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(NormalizedStringToEnumConverterFactory.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("AI prompt admin controller tests: ")
class AiPromptAdminControllerTest {

    private static final String PROMPT_ID =
            "6a462f4da54bd47af37800eb";

    private static final FeatureName FEATURE =
            FeatureName.MEDICAL_SUMMARY;

    private static final PromptType TYPE =
            PromptType.SYSTEM;

    private static final AiProviderModel TARGET_MODEL =
            AiProviderModel.LLAMA_3;

    private static final Instant CREATED_AT =
            Instant.parse("2026-07-02T10:15:30Z");

    private static final Instant UPDATED_AT =
            Instant.parse("2026-07-03T14:20:10Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiPromptManagementService service;

    @Test
    void createPrompt_ShouldReturn201_WhenRequestIsValid() throws Exception {
        CreateAiPromptRequest request = createRequest();

        AiPromptDetailsResponse response =
                createDetailsResponse(
                        PROMPT_ID,
                        1L,
                        false
                );

        when(service.createPrompt(request))
                .thenReturn(response);

        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(PROMPT_ID))
                .andExpect(jsonPath("$.feature")
                        .value("MEDICAL_SUMMARY"))
                .andExpect(jsonPath("$.type")
                        .value("SYSTEM"))
                .andExpect(jsonPath("$.targetModel")
                        .value("LLAMA_3"))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.content")
                        .value(request.content()))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.description")
                        .value(request.promptDescription()))
                .andExpect(jsonPath("$.versionComment")
                        .value(request.versionComment()))
                .andExpect(jsonPath("$.createdByUserId")
                        .value("system"))
                .andExpect(jsonPath("$.createdByUsername")
                        .value("system"))
                .andExpect(jsonPath("$.createdAt")
                        .value(CREATED_AT.toString()));

        verify(service).createPrompt(request);
    }

    @Test
    void activatePrompt_ShouldReturn200_WhenPromptExists() throws Exception {
        AiPromptDetailsResponse response =
                createDetailsResponse(
                        PROMPT_ID,
                        2L,
                        true
                );

        when(service.activatePrompt(PROMPT_ID))
                .thenReturn(response);

        mockMvc.perform(patch(ACTIVATE_PROMPT_URL, PROMPT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(PROMPT_ID))
                .andExpect(jsonPath("$.version").value(2))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.updatedByUserId")
                        .value("system"))
                .andExpect(jsonPath("$.updatedByUsername")
                        .value("system"))
                .andExpect(jsonPath("$.updatedAt")
                        .value(UPDATED_AT.toString()));

        verify(service).activatePrompt(PROMPT_ID);
    }

    @Test
    void getPrompt_ShouldReturn200_WhenPromptExists() throws Exception {
        AiPromptDetailsResponse response =
                createDetailsResponse(
                        PROMPT_ID,
                        3L,
                        false
                );

        when(service.getPrompt(PROMPT_ID))
                .thenReturn(response);

        mockMvc.perform(get(PROMPT_BY_ID_URL, PROMPT_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(PROMPT_ID))
                .andExpect(jsonPath("$.feature")
                        .value("MEDICAL_SUMMARY"))
                .andExpect(jsonPath("$.type")
                        .value("SYSTEM"))
                .andExpect(jsonPath("$.targetModel")
                        .value("LLAMA_3"))
                .andExpect(jsonPath("$.version").value(3))
                .andExpect(jsonPath("$.active").value(false));

        verify(service).getPrompt(PROMPT_ID);
    }

    @Test
    void getPromptVersions_ShouldReturn200AndVersions() throws Exception {
        AiPromptResponse versionTwo =
                createPromptResponse(
                        "prompt-v2",
                        2L,
                        true
                );

        AiPromptResponse versionOne =
                createPromptResponse(
                        "prompt-v1",
                        1L,
                        false
                );

        AiPromptKey expectedKey =
                new AiPromptKey(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                );

        when(service.getPromptVersions(expectedKey))
                .thenReturn(List.of(
                        versionTwo,
                        versionOne
                ));

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", "MEDICAL_SUMMARY")
                        .param("type", "SYSTEM")
                        .param("targetModel", "LLAMA_3"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id")
                        .value("prompt-v2"))
                .andExpect(jsonPath("$[0].version")
                        .value(2))
                .andExpect(jsonPath("$[0].active")
                        .value(true))
                .andExpect(jsonPath("$[1].id")
                        .value("prompt-v1"))
                .andExpect(jsonPath("$[1].version")
                        .value(1))
                .andExpect(jsonPath("$[1].active")
                        .value(false));

        verify(service).getPromptVersions(expectedKey);
    }

    @Test
    void getCurrentPrompt_ShouldReturn200_WhenActivePromptExists()
            throws Exception {

        AiPromptDetailsResponse response =
                createDetailsResponse(
                        PROMPT_ID,
                        2L,
                        true
                );

        AiPromptKey expectedKey =
                new AiPromptKey(
                        FEATURE,
                        TYPE,
                        TARGET_MODEL
                );

        when(service.getActivePrompt(expectedKey))
                .thenReturn(response);

        mockMvc.perform(get(CURRENT_PROMPT_URL)
                        .param("feature", "MEDICAL_SUMMARY")
                        .param("type", "SYSTEM")
                        .param("targetModel", "LLAMA_3"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id")
                        .value(PROMPT_ID))
                .andExpect(jsonPath("$.feature")
                        .value("MEDICAL_SUMMARY"))
                .andExpect(jsonPath("$.type")
                        .value("SYSTEM"))
                .andExpect(jsonPath("$.targetModel")
                        .value("LLAMA_3"))
                .andExpect(jsonPath("$.version")
                        .value(2))
                .andExpect(jsonPath("$.active")
                        .value(true));

        verify(service).getActivePrompt(expectedKey);
    }

    @Test
    void createPrompt_ShouldReturn400_WhenBodyIsInvalid() throws Exception {
        String invalidRequest = """
                {
                  "feature": null,
                  "type": null,
                  "targetModel": null,
                  "content": "   ",
                  "promptDescription": "Description",
                  "versionComment": "Comment"
                }
                """;

        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createPrompt_ShouldReturn400_WhenContentIsTooShort() throws Exception {
        String invalidRequest = """
                {
                  "feature": "MEDICAL_SUMMARY",
                  "type": "SYSTEM",
                  "targetModel": "LLAMA_3",
                  "content": "short",
                  "promptDescription": "Description",
                  "versionComment": "Comment"
                }
                """;

        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createPrompt_ShouldReturn400_WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post(PROMPTS_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getPromptVersions_ShouldReturn400_WhenFeatureIsMissing()
            throws Exception {

        mockMvc.perform(get(PROMPTS_URL)
                        .param("type", "SYSTEM")
                        .param("targetModel", "LLAMA_3"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getPromptVersions_ShouldReturn400_WhenTypeIsMissing()
            throws Exception {

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", "MEDICAL_SUMMARY")
                        .param("targetModel", "LLAMA_3"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getPromptVersions_ShouldReturn400_WhenTargetModelIsMissing()
            throws Exception {

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", "MEDICAL_SUMMARY")
                        .param("type", "SYSTEM"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getCurrentPrompt_ShouldReturn400_WhenRequiredParametersAreMissing()
            throws Exception {

        mockMvc.perform(get(CURRENT_PROMPT_URL))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getPromptVersions_ShouldReturn400_WhenFeatureIsBlank()
            throws Exception {

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", "")
                        .param("type", "SYSTEM")
                        .param("targetModel", "LLAMA_3"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getPromptVersions_ShouldReturn400_WhenEnumValueIsInvalid()
            throws Exception {

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", "UNKNOWN_FEATURE")
                        .param("type", "SYSTEM")
                        .param("targetModel", "LLAMA_3"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void getPromptVersions_ShouldNormalizeEnumParameters()
            throws Exception {

        AiPromptKey expectedKey =
                new AiPromptKey(
                        FeatureName.MEDICAL_SUMMARY,
                        PromptType.SYSTEM,
                        AiProviderModel.LLAMA_3
                );

        when(service.getPromptVersions(expectedKey))
                .thenReturn(List.of());

        mockMvc.perform(get(PROMPTS_URL)
                        .param("feature", "medical-summary")
                        .param("type", "system")
                        .param("targetModel", "llama_3"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.length()").value(0));

        ArgumentCaptor<AiPromptKey> keyCaptor =
                ArgumentCaptor.forClass(AiPromptKey.class);

        verify(service).getPromptVersions(
                keyCaptor.capture()
        );

        assertThat(keyCaptor.getValue())
                .isEqualTo(expectedKey);
    }

    @Test
    void getCurrentPrompt_ShouldNormalizeEnumParameters()
            throws Exception {

        AiPromptKey expectedKey =
                new AiPromptKey(
                        FeatureName.MEDICAL_SUMMARY,
                        PromptType.SYSTEM,
                        AiProviderModel.LLAMA_3
                );

        AiPromptDetailsResponse response =
                createDetailsResponse(
                        PROMPT_ID,
                        2L,
                        true
                );

        when(service.getActivePrompt(expectedKey))
                .thenReturn(response);

        mockMvc.perform(get(CURRENT_PROMPT_URL)
                        .param("feature", "medical-summary")
                        .param("type", "system")
                        .param("targetModel", "llama_3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(PROMPT_ID))
                .andExpect(jsonPath("$.active")
                        .value(true));

        verify(service).getActivePrompt(expectedKey);
    }

    private CreateAiPromptRequest createRequest() {
        return new CreateAiPromptRequest(
                FEATURE,
                TYPE,
                TARGET_MODEL,
                """
                You are a medical summary assistant.
                Return only information explicitly stated in the note.
                """,
                "Medical Summary System Prompt",
                "Initial production version"
        );
    }

    private AiPromptDetailsResponse createDetailsResponse(
            String id,
            long version,
            boolean active
    ) {
        return new AiPromptDetailsResponse(
                id,
                FEATURE,
                TYPE,
                TARGET_MODEL,
                version,
                """
                You are a medical summary assistant.
                Return only information explicitly stated in the note.
                """,
                active,
                "Medical Summary System Prompt",
                "Initial production version",
                "system",
                "system",
                active ? "system" : null,
                active ? "system" : null,
                CREATED_AT,
                active ? UPDATED_AT : null
        );
    }

    private AiPromptResponse createPromptResponse(
            String id,
            long version,
            boolean active
    ) {
        return new AiPromptResponse(
                id,
                FEATURE,
                TYPE,
                TARGET_MODEL,
                version,
                active,
                "system",
                CREATED_AT,
                active ? UPDATED_AT : null
        );
    }
}