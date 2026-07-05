package com.healthcare.aiservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.healthcare.aiservice.common.medical_extraction.controller.API.MedicalInfoExtractionApiPaths.EXTRACT_MEDICAL_INFO_URL;
import static com.healthcare.aiservice.common.medical_summary.controller.API.MedicalSummaryApiPaths.MEDICAL_NOTE_SUMMARY_URL;
import static com.healthcare.aiservice.common.message_classification.controller.API.MessageClassificationApiPaths.CLASSIFY_MESSAGE_URL;
import static com.healthcare.aiservice.common.prompt.controller.API.AiPromptApiPaths.*;
import static com.healthcare.aiservice.common.statistics.controller.API.AiStatisticsApiPaths.STATISTICS_ADMIN_URL;

@Configuration
public class OpenApiConfig {

    private static final String TIMESTAMP_EXAMPLE = "2028-07-21T11:20:00Z";
//    private final String BEARER_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {

        Components components = new Components()
//                .addSecuritySchemes(BEARER_SCHEME_NAME, createAPIKeyScheme())

                // 400
                .addExamples("Error400ValidationMedicalSummary", ex400ValidationNote(MEDICAL_NOTE_SUMMARY_URL))
                .addExamples("Error400ValidationMessageClassification", ex400ValidationNote(CLASSIFY_MESSAGE_URL))
                .addExamples("Error400ValidationMedicalInfoExtraction", ex400ValidationNote(EXTRACT_MEDICAL_INFO_URL))

                .addExamples("Error400CreatePrompt", ex400InvalidEnum(
                        PROMPTS_URL,
                        "targetModel",
                        "LLAMA3",
                        List.of("LLAMA_3", "GEMINI", "CLAUDE", "GPT_5")
                ))
                .addExamples("Error400ActivatePrompt", ex400InvalidField(
                        ACTIVATE_PROMPT_URL,
                        "promptId",
                        "Prompt id must not be blank."
                ))
                .addExamples("Error400GetPromptVersions", ex400InvalidEnum(
                        PROMPTS_URL,
                        "feature",
                        "SUMMARY",
                        List.of("MEDICAL_SUMMARY", "MESSAGE_CLASSIFICATION", "MEDICAL_INFO_EXTRACTION")
                ))
                .addExamples("Error400GetCurrentPrompt", ex400InvalidEnum(
                        CURRENT_PROMPT_URL,
                        "targetModel",
                        "LLAMA3",
                        List.of("LLAMA_3", "GEMINI", "CLAUDE", "GPT_5")
                ))

                // 404
                .addExamples("Error404PromptById", ex404(
                        PROMPT_BY_ID_URL,
                        "AI prompt with id '6a462f4da54bd47af37800eb' was not found."
                ))
                .addExamples("Error404ActivatePrompt", ex404(
                        ACTIVATE_PROMPT_URL,
                        "AI prompt with id '6a462f4da54bd47af37800eb' was not found."
                ))
                .addExamples("Error404GetPromptVersions", ex404(
                        PROMPTS_URL,
                        "No AI prompts found for feature 'MEDICAL_SUMMARY', type 'SYSTEM' and target model 'LLAMA_3'."
                ))
                .addExamples("Error404GetCurrentPrompt", ex404(
                        CURRENT_PROMPT_URL,
                        "No active AI prompt found for feature 'MEDICAL_SUMMARY', type 'SYSTEM' and target model 'LLAMA_3'."
                ))

                // 409
                .addExamples("Error409CreatePrompt", ex409Prompt(PROMPTS_URL))

                // 502
                .addExamples("Error502AiResponseParsingMedicalSummary", ex502AiResponseParsing(MEDICAL_NOTE_SUMMARY_URL))
                .addExamples("Error502AiResponseParsingMessageClassification", ex502AiResponseParsing(CLASSIFY_MESSAGE_URL))
                .addExamples("Error502AiResponseMedicalInfoExtraction", ex502AiResponseParsing(EXTRACT_MEDICAL_INFO_URL))

                // 503
                .addExamples("Error503AiProviderUnavailableMedicalSummary", ex503(MEDICAL_NOTE_SUMMARY_URL))
                .addExamples("Error503AiProviderUnavailableMessageClassification", ex503(CLASSIFY_MESSAGE_URL))
                .addExamples("Error503AiProviderUnavailableMedicalInfoExtraction", ex503(EXTRACT_MEDICAL_INFO_URL))

                // 500
                .addExamples("Error500InternalServerErrorMedicalSummary", ex500(MEDICAL_NOTE_SUMMARY_URL))
                .addExamples("Error500InternalServerErrorMessageClassification", ex500(CLASSIFY_MESSAGE_URL))
                .addExamples("Error500InternalServerErrorMedicalInfoExtraction", ex500(EXTRACT_MEDICAL_INFO_URL))
                .addExamples("Error500InternalServerErrorStatistics", ex500(STATISTICS_ADMIN_URL))
                .addExamples("Error500InternalServerErrorCreatePrompt", ex500(PROMPTS_URL))
                .addExamples("Error500InternalServerErrorPromptById", ex500(PROMPT_BY_ID_URL))
                .addExamples("Error500InternalServerErrorActivatePrompt", ex500(ACTIVATE_PROMPT_URL))
                .addExamples("Error500InternalServerErrorGetPromptVersions", ex500(PROMPTS_URL))
                .addExamples("Error500InternalServerErrorGetCurrentPrompt", ex500(CURRENT_PROMPT_URL));

        return new OpenAPI()
//                .addSecurityItem(new SecurityRequirement()
//                        .addList(BEARER_SCHEME_NAME))
                .components(components);
    }

//    private SecurityScheme createAPIKeyScheme() {
//        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
//                .bearerFormat("JWT")
//                .scheme("bearer");
//    }

    private static Map<String, Object> errorExample(HttpStatus status,
                                                    String path,
                                                    String message) {
        return errorExample(status, path, message, List.of());
    }

    private static Map<String, Object> errorExample(HttpStatus status,
                                                    String path,
                                                    String message,
                                                    List<Map<String, Object>> validationErrors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", TIMESTAMP_EXAMPLE);
        body.put("status", status.value());
        body.put("error", status.name());
        body.put("message", message);
        body.put("path", path);
        body.put("validationErrors", validationErrors);

        return body;
    }

    private static Map<String, Object> validationError(String field, String message) {
        Map<String, Object> validationError = new LinkedHashMap<>();
        validationError.put("field", field);
        validationError.put("message", message);

        return validationError;
    }

    private static Example ex(String summary, String description, Map<String, Object> value) {
        return new Example()
                .summary(summary)
                .description(description)
                .value(value);
    }

    private static Example ex400ValidationNote(String url) {
        String message = "Note must not be blank.";

        return ex(
                message,
                message,
                errorExample(
                        HttpStatus.BAD_REQUEST,
                        url,
                        message,
                        List.of(validationError("note", message))
                )
        );
    }

    private static Example ex400InvalidField(String url, String field, String message) {
        return ex(
                message,
                message,
                errorExample(
                        HttpStatus.BAD_REQUEST,
                        url,
                        message,
                        List.of(validationError(field, message))
                )
        );
    }

    private static Example ex400InvalidEnum(String url,
                                            String field,
                                            String invalidValue,
                                            List<String> allowedValues) {
        String message = String.format(
                "Invalid value '%s' for field '%s'. Allowed values: [%s].",
                invalidValue,
                field,
                String.join(", ", allowedValues)
        );

        return ex(
                message,
                message,
                errorExample(HttpStatus.BAD_REQUEST, url, message)
        );
    }

    private static Example ex404(String url, String message) {
        return ex(
                message,
                message,
                errorExample(HttpStatus.NOT_FOUND, url, message)
        );
    }

    private static Example ex409Prompt(String url) {
        String message = "AI prompt version '1' already exists for feature 'MEDICAL_SUMMARY', type 'SYSTEM' and target model 'LLAMA_3'.";

        return ex(
                message,
                message,
                errorExample(HttpStatus.CONFLICT, url, message)
        );
    }

    private static Example ex502AiResponseParsing(String url) {
        String message = "AI returned response in an unexpected format.";

        return ex(
                message,
                message,
                errorExample(HttpStatus.BAD_GATEWAY, url, message)
        );
    }

    private static Example ex503(String url) {
        String message = "The AI provider is currently overloaded or under maintenance. Please try again later.";

        return ex(
                message,
                message,
                errorExample(HttpStatus.SERVICE_UNAVAILABLE, url, message)
        );
    }

    private static Example ex500(String url) {
        String message = "Temporary service error.";

        return ex(
                message,
                message,
                errorExample(HttpStatus.INTERNAL_SERVER_ERROR, url, message)
        );
    }
}