package com.healthcare.aiservice.common.statistics.controller.API;

import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.exception.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_ADMIN_URL;
import static com.healthcare.aiservice.common.statistics.contriller.API.AiStatisticsApiPaths.STATISTICS;


@RequestMapping(AI_BASIC_ADMIN_URL)
@Tag(
        name = "AI Admin Statistics Controller",
        description = "Controller for AI service usage and performance statistics"
)
public interface AiStatisticsAPI {

    @Operation(
            summary = "Get AI service statistics",
            description = """
                    Returns aggregated AI service statistics.

                    The response contains:

                    - total number of AI requests
                    - successful requests
                    - failed requests
                    - average request duration
                    - number of requests grouped by AI feature

                    This endpoint is intended for monitoring and administration purposes.
                    """
    )
    @ApiResponses(value = {

            @ApiResponse(
                    responseCode = "200",
                    description = "AI statistics retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AiStatisticsResponse.class)
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
                                            name = "Error500InternalServerErrorStatistics",
                                            ref = "#/components/examples/Error500InternalServerErrorStatistics"
                                    )
                            }
                    )
            )
    })
    @GetMapping(STATISTICS)
    ResponseEntity<AiStatisticsResponse> getStatistics();
}