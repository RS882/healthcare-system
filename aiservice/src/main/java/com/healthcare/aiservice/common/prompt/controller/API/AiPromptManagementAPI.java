package com.healthcare.aiservice.common.prompt.controller.API;

import com.healthcare.aiservice.common.prompt.dto.AiPromptDetailsResponse;
import com.healthcare.aiservice.common.prompt.dto.AiPromptResponse;
import com.healthcare.aiservice.common.prompt.dto.CreateAiPromptRequest;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_ADMIN_URL;
import static com.healthcare.aiservice.common.prompt.controller.API.AiPromptApiPaths.*;

@RequestMapping(AI_BASIC_ADMIN_URL)
@Tag(
        name = "AI Prompt Management",
        description = "Administration of AI prompt versions"
)
@Validated
public interface AiPromptManagementAPI {

    @Operation(
            summary = "Create AI prompt",
            description = "Creates a new inactive prompt version.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request for creating a new AI prompt version",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CreateAiPromptRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "AI prompt created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AiPromptDetailsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400CreatePrompt",
                                            ref = "#/components/examples/Error400CreatePrompt"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "AI prompt version already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error409CreatePrompt",
                                            ref = "#/components/examples/Error409CreatePrompt"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500InternalServerErrorCreatePrompt",
                                            ref = "#/components/examples/Error500InternalServerErrorCreatePrompt"
                                    )
                            }
                    )
            )
    })
    @PostMapping(PROMPTS)
    ResponseEntity<AiPromptDetailsResponse> createPrompt(
            @Valid
            @RequestBody
            CreateAiPromptRequest request
    );


    @Operation(
            summary = "Activate prompt version",
            description = "Marks the selected prompt version as active and deactivates" +
                    " all other versions for the same feature, prompt type and target model."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "AI prompt activated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AiPromptDetailsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid prompt id",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400ActivatePrompt",
                                            ref = "#/components/examples/Error400ActivatePrompt"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "AI prompt not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error404ActivatePrompt",
                                            ref = "#/components/examples/Error404ActivatePrompt"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500InternalServerErrorActivatePrompt",
                                            ref = "#/components/examples/Error500InternalServerErrorActivatePrompt"
                                    )
                            }
                    )
            )
    })
    @PatchMapping(ACTIVATE_PROMPT)
    ResponseEntity<AiPromptDetailsResponse> activatePrompt(
            @PathVariable(PATH_VARIABLE_PROMPT_ID)
            @NotBlank(message = "Prompt id must not be blank")
            @Parameter(description = "Unique identifier of the prompt version",
                    example = "6a462f4da54bd47af37800eb")
            String promptId
    );


    @Operation(
            summary = "Get prompt by id",
            description = "Returns complete information about a prompt version."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "AI prompt found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AiPromptDetailsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "AI prompt not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error404PromptById",
                                            ref = "#/components/examples/Error404PromptById"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500InternalServerErrorPromptById",
                                            ref = "#/components/examples/Error500InternalServerErrorPromptById"
                                    )
                            }
                    )
            )
    })
    @GetMapping(PROMPT_BY_ID)
    ResponseEntity<AiPromptDetailsResponse> getPrompt(
            @PathVariable(PATH_VARIABLE_PROMPT_ID)
            @NotBlank(message = "Prompt id must not be blank")
            @Parameter(description = "Unique identifier of the prompt version",
                    example = "6a462f4da54bd47af37800eb")
            String promptId
    );


    @Operation(
            summary = "Get prompt versions",
            description = "Returns all prompt versions for the specified feature, prompt type and target model ordered by version descending."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "AI prompt versions found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(implementation = AiPromptResponse.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid query parameters",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400GetPromptVersions",
                                            ref = "#/components/examples/Error400GetPromptVersions"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No AI prompt versions found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error404GetPromptVersions",
                                            ref = "#/components/examples/Error404GetPromptVersions"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500InternalServerErrorGetPromptVersions",
                                            ref = "#/components/examples/Error500InternalServerErrorGetPromptVersions"
                                    )
                            }
                    )
            )
    })
    @GetMapping(PROMPTS)
    ResponseEntity<List<AiPromptResponse>> getPromptVersions(
            @RequestParam
            @NotNull(message = "Feature name must not be null")
            @Parameter(description = "Name of feature", example = "MEDICAL_SUMMARY")
            FeatureName feature,

            @RequestParam
            @NotNull(message = "Prompt type must not be null")
            @Parameter(description = "Type of prompt", example = "SYSTEM")
            PromptType type,

            @RequestParam
            @NotNull(message = "Target AI model must not be null")
            @Parameter(description = "AI model", example = "LLAMA_3")
            AiProviderModel targetModel
    );


    @Operation(
            summary = "Get current prompt",
            description = "Returns the currently active prompt for the specified feature, prompt type and target model."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current active AI prompt found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AiPromptDetailsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid query parameters",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error400GetCurrentPrompt",
                                            ref = "#/components/examples/Error400GetCurrentPrompt"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Current active AI prompt not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error404GetCurrentPrompt",
                                            ref = "#/components/examples/Error404GetCurrentPrompt"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Error500InternalServerErrorGetCurrentPrompt",
                                            ref = "#/components/examples/Error500InternalServerErrorGetCurrentPrompt"
                                    )
                            }
                    )
            )
    })
    @GetMapping(CURRENT_PROMPT)
    ResponseEntity<AiPromptDetailsResponse> getCurrentPrompt(
            @RequestParam
            @NotNull(message = "Feature name must not be null")
            @Parameter(description = "Name of feature", example = "MEDICAL_SUMMARY")
            FeatureName feature,

            @RequestParam
            @NotNull(message = "Prompt type must not be null")
            @Parameter(description = "Type of prompt", example = "SYSTEM")
            PromptType type,

            @RequestParam
            @NotNull(message = "Target AI model must not be null")
            @Parameter(description = "AI model", example = "LLAMA_3")
            AiProviderModel targetModel
    );
}
