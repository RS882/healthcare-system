package com.healthcare.aiservice.common.statistics.contriller;

import com.healthcare.aiservice.common.statistics.contriller.API.AiStatisticsAPI;
import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.common.statistics.service.interfaces.AiStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AiStatisticsController implements AiStatisticsAPI {

    private final AiStatisticService service;

    @Override
    public ResponseEntity<AiStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(service.getStatistic());
    }
}
