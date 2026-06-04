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

import static com.healthcare.aiservice.common.medical_summary.controller.API.MedicalSummaryApiPaths.MEDICAL_NOTE_SUMMARY_URL;
import static com.healthcare.aiservice.common.message_classification.controller.API.MessageClassificationApiPaths.CLASSIFY_MESSAGE_URL;


@Configuration
public class OpenApiConfig {

    private static final String TIMESTAMP_EXAMPLE = "2028-07-21T11:20:00Z";
//    private final String BEARER_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {

        Components components = new Components()
//                .addSecuritySchemes(BEARER_SCHEME_NAME, createAPIKeyScheme())
                // 400
                .addExamples("Error400ValidationMedicalSummary", ex400Validation(MEDICAL_NOTE_SUMMARY_URL))
                .addExamples("Error400ValidationMessageClassification", ex400Validation(CLASSIFY_MESSAGE_URL))
                // 502
                .addExamples("Error502AiResponseParsingMedicalSummary", ex502AiResponseParsing(MEDICAL_NOTE_SUMMARY_URL))
                .addExamples("Error502AiResponseParsingMessageClassification", ex502AiResponseParsing(CLASSIFY_MESSAGE_URL))
                // 503
                .addExamples("Error503AiProviderUnavailableMedicalSummary", ex503(MEDICAL_NOTE_SUMMARY_URL))
                .addExamples("Error503AiProviderUnavailableMessageClassification", ex503(CLASSIFY_MESSAGE_URL))
                // 500
                .addExamples("Error500InternalServerErrorMedicalSummary", ex500(MEDICAL_NOTE_SUMMARY_URL))
                .addExamples("Error500InternalServerErrorMessageClassification", ex500(CLASSIFY_MESSAGE_URL))
                ;

        return new OpenAPI()
//                .addSecurityItem(new SecurityRequirement().
//                        addList(BEARER_SCHEME_NAME))
                .components(components);
    }

//    private SecurityScheme createAPIKeyScheme() {
//        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
//                .bearerFormat("JWT")
//                .scheme("bearer");
//    }

    private static Map<String, Object> errorExample(HttpStatus status,
                                                    String path,
                                                    String topMessage) {
        return errorExample(status, path, topMessage, null, null);
    }

    private static Map<String, Object> errorExample(HttpStatus status,
                                                    String path,
                                                    String topMessage,
                                                    String field,
                                                    String fieldMessage) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", TIMESTAMP_EXAMPLE);
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", List.of(topMessage));
        body.put("path", path);

        if (field != null && fieldMessage != null) {
            Map<String, Object> ve = new LinkedHashMap<>();
            ve.put("field", field);
            ve.put("message", fieldMessage);
            body.put("validationErrors", List.of(ve));
        } else {
            body.put("validationErrors", null);
        }
        return body;
    }

    private static Example ex(String summary, String description, Map<String, Object> value) {
        return new Example().summary(summary).description(description).value(value);
    }

    private static Example ex400Validation(String url) {
        return ex(
                "Note must not be blank.",
                "Note must not be blank.",
                errorExample(HttpStatus.BAD_REQUEST, url,
                        "Note must not be blank.",
                        "note", "Note must not be blank.")
        );
    }

    private static Example ex502AiResponseParsing(String url) {
        return ex(
                "AI returned response in an unexpected format",
                "AI returned response in an unexpected format",
                errorExample(HttpStatus.BAD_GATEWAY, url,
                        "AI returned response in an unexpected format")
        );
    }

    private static Example ex500(String url) {
        return ex(
                "Temporary service error.",
                "Temporary service error.",
                errorExample(HttpStatus.INTERNAL_SERVER_ERROR, url,
                        "Temporary service error."));
    }

    private static Example ex503(String url) {
        return ex(
                "AI Provider unavailable.",
                "AI Provider.",
                errorExample(HttpStatus.SERVICE_UNAVAILABLE, url,
                        "The AI Provider is currently overloaded or under maintenance. Please try again later."));
    }
}




