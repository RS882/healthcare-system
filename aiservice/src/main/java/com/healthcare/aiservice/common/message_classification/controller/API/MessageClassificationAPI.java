package com.healthcare.aiservice.common.message_classification.controller.API;


import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationResponse;
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
import com.healthcare.aiservice.exception.dto.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_URL;
import static com.healthcare.aiservice.common.message_classification.controller.API.MessageClassificationApiPaths.CLASSIFY_MESSAGE;


@RequestMapping(AI_BASIC_URL)
@Tag(name = "Patient message classification controller", description = "Controller for patient message AI-classification")
public interface MessageClassificationAPI {
    @Operation(
            summary = "Classify patient message",
            description = """
                Classifies a patient message into one healthcare-related category.
                
                The endpoint uses the configured AI model to detect the intent of the message,
                such as appointment requests, prescription questions, symptoms, insurance-related
                questions, emergency cases, or other general messages.
                
                Only information explicitly contained in the provided message should be used.
                """
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "Patient message classified successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageClassificationResponse.class)
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
                                            name = "Error400ValidationMessageClassification",
                                            ref = "#/components/examples/Error400ValidationMessageClassification"
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
                                            name = "Error502AiResponseParsingMessageClassification",
                                            ref = "#/components/examples/Error502AiResponseParsingMessageClassification"
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
                                            name = "Error503AiProviderUnavailableMessageClassification",
                                            ref = "#/components/examples/Error503AiProviderUnavailableMessageClassification"
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
                                            name = "Error500InternalServerErrorMessageClassification",
                                            ref = "#/components/examples/Error500InternalServerErrorMessageClassification"
                                    )
                            }
                    )
            )
    })
    @PostMapping(CLASSIFY_MESSAGE)
    ResponseEntity<MessageClassificationResponse> classify(
            @Valid
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Patient message classification request",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MessageClassificationRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            MessageClassificationRequest request
    );
}
