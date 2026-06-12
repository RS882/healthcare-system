package com.healthcare.aiservice.common.medical_extraction.controller.API;

import static com.healthcare.aiservice.common.APIPaths.ApiPaths.AI_BASIC_URL;

public final class MedicalInfoExtractionApiPaths {
    private MedicalInfoExtractionApiPaths() {
    }

    public static final String EXTRACT_MEDICAL_INFO = "/extract-medical-info";

    public static final String EXTRACT_MEDICAL_INFO_URL = AI_BASIC_URL+ EXTRACT_MEDICAL_INFO;
}
