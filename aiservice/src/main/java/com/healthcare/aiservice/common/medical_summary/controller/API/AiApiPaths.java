package com.healthcare.aiservice.common.medical_summary.controller.API;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_URL;

public final class AiApiPaths {

    private AiApiPaths() {
    }

    public static final String MEDICAL_NOTE = "/medical-note";

    public static final String SUMMARY =  "/summary";

    public static final String MEDICAL_NOTE_BASIC_URL = AI_BASIC_URL + MEDICAL_NOTE;

    public static final String MEDICAL_NOTE_SUMMARY_URL = MEDICAL_NOTE_BASIC_URL+ SUMMARY;

}
