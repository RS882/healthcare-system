package com.healthcare.aiservice.common.medical_summary.controller.API;


import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.aiservice.common.medical_summary.controller.API.MedicalSummaryApiPaths.MEDICAL_NOTE_BASIC_URL;
import static com.healthcare.aiservice.common.medical_summary.controller.API.MedicalSummaryApiPaths.SUMMARY;


@RequestMapping(MEDICAL_NOTE_BASIC_URL)
@Tag(name = "Medical summary controller", description = "Controller for medical note AI-summarization")
public interface MedicalSummaryAPI {

    @Operation(
            summary = "Summarize medical note",
            description = """
                Generates an AI-powered summary of a medical note and extracts
                structured medical information such as diagnoses, medications,
                and recommendations.
                
                The response is generated using a configured AI model.
                Only information explicitly contained in the provided medical note
                should be included in the result.
                """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MedicalSummaryRequest.class)
                    )
            )
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "Medical note summarized successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MedicalSummaryResponse.class)
                    )
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400Validation",
                                            ref = "#/components/examples/Error400Validation"
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "502",
                    description = "AI provider returned invalid response format",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error502AiResponseParsing",
                                            ref = "#/components/examples/Error502AiResponseParsing"
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "503",
                    description = "AI provider unavailable",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error503AiProviderUnavailable",
                                            ref = "#/components/examples/Error503AiProviderUnavailable"
                                    )
                            }
                    )
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500InternalServerError",
                                            ref = "#/components/examples/Error500InternalServerError"
                                    )
                            }
                    )
            )
    })
    @PostMapping(SUMMARY)
    ResponseEntity<MedicalSummaryResponse> summarize(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            MedicalSummaryRequest request);
}
