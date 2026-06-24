package com.healthcare.aiservice.common.statistics.contriller.API;

import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_ADMIN_URL;
import static com.healthcare.aiservice.common.statistics.contriller.API.AiStatisticsApiPaths.STATISTICS;


@RequestMapping(AI_BASIC_ADMIN_URL)
@Tag(name = "AI Admin Statistics", description = "AI service usage and performance statistics")
public interface AiStatisticsAPI {

    @GetMapping(STATISTICS)
    @Operation(
            summary = "Get AI service statistics",
            description = "Returns total AI requests, successful and failed requests, average duration, and requests grouped by feature."
    )
    ResponseEntity<AiStatisticsResponse> getStatistics();
}
