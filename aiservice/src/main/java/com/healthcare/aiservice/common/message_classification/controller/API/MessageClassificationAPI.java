package com.healthcare.aiservice.common.message_classification.controller.API;


import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationRequest;
import com.healthcare.aiservice.common.message_classification.dto.MessageClassificationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_URL;
import static com.healthcare.aiservice.common.message_classification.controller.API.MessageClassificationApiPaths.CLASSIFY_MESSAGE;


@RequestMapping(AI_BASIC_URL)
@Tag(name = "Patient message classification controller", description = "Controller for patient message AI-classification")
public interface MessageClassificationAPI {

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
    @PostMapping(CLASSIFY_MESSAGE)
    ResponseEntity<MessageClassificationResponse> classify(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            MessageClassificationRequest request);
}
