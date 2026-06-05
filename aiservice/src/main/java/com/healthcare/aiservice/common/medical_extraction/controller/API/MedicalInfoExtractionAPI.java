package com.healthcare.aiservice.common.medical_extraction.controller.API;

import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionRequest;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionResponse;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.exception.dto.ErrorResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_URL;
import static com.healthcare.aiservice.common.medical_extraction.controller.API.MedicalInfoExtractionApiPaths.EXTRACT_MEDICAL_INFO;

@RequestMapping(AI_BASIC_URL)
@Tag(name = "Medical info extraction controller", description = "Controller for extraction medical info from the medical note")
public interface MedicalInfoExtractionAPI {

    @Operation(
            summary = "Extract structured medical information",
            description = """
                    Extracts symptoms, diagnoses, medications, allergies,
                    procedures, and recommendations from a medical note.
                    
                    Note:
                    For JSON requests, line breaks inside the note field must
                    be escaped using \\n.
                    """,
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MedicalInfoExtractionRequest.class)
            )
    )
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "Medical info extracted successfully",
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
                                            name = "Error400ValidationMedicalInfoExtraction",
                                            ref = "#/components/examples/Error400ValidationMedicalInfoExtraction"
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
                                            name = "Error502AiResponseMedicalInfoExtraction",
                                            ref = "#/components/examples/Error502AiResponseMedicalInfoExtraction"
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
                                            name = "Error503AiProviderUnavailableMedicalInfoExtraction",
                                            ref = "#/components/examples/Error503AiProviderUnavailableMedicalInfoExtraction"
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
                                            name = "Error500InternalServerErrorMedicalInfoExtraction",
                                            ref = "#/components/examples/Error500InternalServerErrorMedicalInfoExtraction"
                                    )
                            }
                    )
            )
    })
    @PostMapping(EXTRACT_MEDICAL_INFO)
    ResponseEntity<MedicalInfoExtractionResponse> extract(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Medical note text to analyze",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MedicalInfoExtractionRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            MedicalInfoExtractionRequest request
    );
}
