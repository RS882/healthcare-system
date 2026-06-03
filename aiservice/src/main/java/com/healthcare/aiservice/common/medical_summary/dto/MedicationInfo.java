package com.healthcare.aiservice.common.medical_summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Medication information")
public record MedicationInfo(

        @Schema(
                description = "Medication name",
                example = "Metformin"
        )
        String name,

        @Schema(
                description = "Medication dosage",
                example = "1000 mg twice daily"
        )
        String dosage

) {
}
