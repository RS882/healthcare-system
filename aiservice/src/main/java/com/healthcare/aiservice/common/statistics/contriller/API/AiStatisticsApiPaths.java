package com.healthcare.aiservice.common.statistics.contriller.API;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_ADMIN_URL;

public final class AiStatisticsApiPaths {

    private AiStatisticsApiPaths() {
    }

    public static final String STATISTICS = "/statistics";

    public static final String STATISTICS_ADMIN_URL = AI_BASIC_ADMIN_URL + STATISTICS;
}
