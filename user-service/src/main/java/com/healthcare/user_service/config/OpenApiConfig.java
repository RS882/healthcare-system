package com.healthcare.user_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.healthcare.user_service.controller.API.ApiPaths.REGISTRATION_URL;

@Configuration
public class OpenApiConfig {

    private final String BEARER_SCHEME_NAME = "Bearer Authentication";
    private static final String TIMESTAMP_EXAMPLE = "2028-07-21T11:20:00Z";

    @Bean
    public OpenAPI openAPI() {

        Components components = new Components()
                .addSecuritySchemes(BEARER_SCHEME_NAME, createAPIKeyScheme())
                // 400
                .addExamples("Error400RegMissingField", ex400Reg())
//                .addExamples("Error400RefreshIncorrectCookie", ex400(REFRESH_URL))
//                .addExamples("Error400LogoutIncorrectCookie", ex400(LOGOUT_URL))
                // 401
//                .addExamples("Error401LoginIncorrectField", ex401Login())
//                .addExamples("Error401RefreshTokenIsIncorrect", ex401(REFRESH_URL))
//                .addExamples("Error401LogoutTokenIsIncorrect", ex401(LOGOUT_URL))
//                .addExamples("Error401ValidationTokenIsIncorrect", ex401(VALIDATION_URL))
                // 403
//                .addExamples("Error403LoginUserIsBlocked", ex403(LOGIN_URL))
                // 404
//                .addExamples("Error404LoginUserIsNotFound", ex404(LOGIN_URL))
//                .addExamples("Error404RefreshUserIsNotFound", ex404(REFRESH_URL))
                // 500
                .addExamples("Error500RegTemporaryServiceError", ex500(REGISTRATION_URL))
//                .addExamples("Error500RefreshTemporaryServiceError", ex500(REFRESH_URL))
//                .addExamples("Error500LogoutTemporaryServiceError", ex500(LOGOUT_URL))
//                .addExamples("Error500ValidationTemporaryServiceError", ex500(VALIDATION_URL))
                // 503
                .addExamples("Error503RegServiceUnavailable", ex503(REGISTRATION_URL))
//                .addExamples("Error503RefreshServiceUnavailable", ex503(REFRESH_URL))
//                .addExamples("Error503ValidationServiceUnavailable", ex503(VALIDATION_URL))
                    ;

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().
                        addList(BEARER_SCHEME_NAME))
                .components(components);
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

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

    private static Example ex400Reg() {
        return ex(
                "Password cannot be empty.",
                "Password should include at least one letter (A-Z or a-z)," +
                        " one digit (0-9), one special character (@, #, $, %, ^, &, +, =, !)," +
                        " have no spaces,no less than 8 characters and no more than 20",
                errorExample(HttpStatus.BAD_REQUEST, REGISTRATION_URL,
                        "Password cannot be empty.",
                        "password", "Password cannot be empty.")
        );
    }

    private static Example ex400(String url) {
        return ex(
                "Cookie is incorrect.",
                "Cookie is incorrect.",
                errorExample(HttpStatus.BAD_REQUEST, url,
                        "Cookie is incorrect."));
    }

    private static Example ex401(String url) {
        return ex(
                "User unauthorized.",
                "User unauthorized.",
                errorExample(HttpStatus.UNAUTHORIZED, url,
                        "Token is incorrect."));
    }

    private static Example ex403(String url) {
        return ex(
                "User is blocked.",
                "User s blocked.",
                errorExample(HttpStatus.FORBIDDEN, url,
                        "User s blocked."));
    }

    private static Example ex404(String url) {
        return ex(
                "User is not found.",
                "User is not found.",
                errorExample(HttpStatus.NOT_FOUND, url,
                        "User is not found."));
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
                "Service unavailable.",
                "Service unavailable.",
                errorExample(HttpStatus.SERVICE_UNAVAILABLE, url,
                        "The server is currently overloaded or under maintenance. Please try again later."));
    }

}




