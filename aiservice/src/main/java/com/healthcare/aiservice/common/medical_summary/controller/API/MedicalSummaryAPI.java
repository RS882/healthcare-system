package com.healthcare.aiservice.common.medical_summary.controller.API;


import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.aiservice.common.medical_summary.controller.API.AiApiPaths.MEDICAL_NOTE_BASIC_URL;
import static com.healthcare.aiservice.common.medical_summary.controller.API.AiApiPaths.SUMMARY;


@RequestMapping(MEDICAL_NOTE_BASIC_URL)
@Tag(name = "Medical summary controller", description = "Controller for medical note AI-summarization")
public interface MedicalSummaryAPI {

    //    @Operation(summary = "Registration new user",
//            description = "This method registers new user from RegistrationDto, returns RegistrationResponse " +
//                    "with user information.",
//            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = RegistrationDto.class)))
//    )
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "User registered successfully",
//                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = RegistrationResponse.class))),
//            @ApiResponse(responseCode = "400", description = "Request is wrong",
//                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ErrorResponse.class),
//                            examples = {
//                                    @ExampleObject(
//                                            name = "Error400RegMissingField",
//                                            ref = "#/components/examples/Error400RegMissingField"
//                                    )
//                            })),
//            @ApiResponse(responseCode = "500",
//                    description = "Temporary error when registering user",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ErrorResponse.class),
//                            examples = {
//                                    @ExampleObject(
//                                            name = "Error500RegTemporaryServiceError",
//                                            ref = "#/components/examples/Error500RegTemporaryServiceError"
//                                    )
//                            }
//                    )),
//            @ApiResponse(responseCode = "503",
//                    description = "The server is currently overloaded or under maintenance.",
//                    content = @Content(
//                            mediaType = MediaType.APPLICATION_JSON_VALUE,
//                            schema = @Schema(implementation = ErrorResponse.class),
//                            examples = {
//                                    @ExampleObject(
//                                            name = "Error503RegServiceUnavailable",
//                                            ref = "#/components/examples/Error503RegServiceUnavailable"
//                                    )
//                            }
//                    ))
//    })
    @PostMapping(SUMMARY)
    ResponseEntity<MedicalSummaryResponse> summarize(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            MedicalSummaryRequest request);
}
