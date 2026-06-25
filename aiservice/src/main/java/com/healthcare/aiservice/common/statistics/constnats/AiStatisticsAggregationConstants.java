package com.healthcare.aiservice.common.statistics.constnats;

public final class AiStatisticsAggregationConstants {
    private AiStatisticsAggregationConstants() {
    }
    public static final String COLLECTION_NAME = "ai_request_logs";

    public static final String FIELD_STATUS = "status";
    public static final String FIELD_FEATURE = "feature";
    public static final String FIELD_DURATION_MS = "durationMs";

    public static final String FACET_TOTAL = "total";
    public static final String FACET_SUCCESSFUL = "successful";
    public static final String FACET_FAILED = "failed";
    public static final String FACET_AVERAGE = "average";
    public static final String FACET_BY_FEATURE = "byFeature";

    public static final String KEY_COUNT = "count";
    public static final String KEY_ID = "_id";
    public static final String KEY_AVERAGE_DURATION_MS = "averageDurationMs";
}
