package com.healthcare.aiservice.common.statistics.service;


import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.common.statistics.service.interfaces.AiStatisticService;
import com.healthcare.aiservice.repository.AiStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Service;

import static com.healthcare.aiservice.common.statistics.mapper.AiStatisticsDocumentMapper.mapToResponse;

@Service
@RequiredArgsConstructor
public class MongoTemplateAiStatisticsService implements AiStatisticService {

    private final AiStatisticsRepository repository;

    @Override
    public AiStatisticsResponse getStatistic() {

        Document statistics = repository.getAiStatistics();

        return statistics == null
                ? AiStatisticsResponse.empty()
                : mapToResponse(statistics);
    }
}