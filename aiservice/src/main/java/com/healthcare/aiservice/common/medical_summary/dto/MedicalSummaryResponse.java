package com.healthcare.aiservice.common.medical_summary.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI-generated medical summary response")
public record MedicalSummaryResponse(

        @Schema(
                description = "Short summary of the medical note",
                example = "Patient reports headache and nausea. Follow-up recommended."
        )
        String summary,

        @ArraySchema(
                schema = @Schema(
                        description = "Detected diagnoses",
                        example = "Migraine"
                )
        )
        List<String> diagnoses,

        @Schema(description = "Detected medications")
        @ArraySchema(
                schema = @Schema(implementation = MedicationInfo.class)
        )
        List<MedicationInfo> medications,

        @ArraySchema(
                schema = @Schema(
                        description = "Medical recommendations",
                        example = "Follow-up in two weeks"
                )
        )
        List<String> recommendations

) {
}