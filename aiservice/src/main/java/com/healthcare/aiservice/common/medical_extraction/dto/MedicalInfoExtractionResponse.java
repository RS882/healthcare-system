package com.healthcare.aiservice.common.medical_extraction.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "MedicalInfoExtractionResponse",
        description = "Structured medical information extracted from the medical note"
)
public record MedicalInfoExtractionResponse(

        @ArraySchema(
                schema = @Schema(
                        description = "Reported symptoms",
                        example = "Fever"
                )
        )
        List<String> symptoms,

        @ArraySchema(
                schema = @Schema(
                        description = "Detected diagnoses",
                        example = "Viral upper respiratory infection"
                )
        )
        List<String> diagnoses,

        @ArraySchema(
                schema = @Schema(
                        description = "Mentioned medications",
                        example = "Paracetamol 500mg"
                )
        )
        List<String> medications,

        @ArraySchema(
                schema = @Schema(
                        description = "Known allergies",
                        example = "Penicillin"
                )
        )
        List<String> allergies,

        @ArraySchema(
                schema = @Schema(
                        description = "Medical procedures or examinations",
                        example = "Chest X-ray"
                )
        )
        List<String> procedures,

        @ArraySchema(
                schema = @Schema(
                        description = "Recommendations or treatment advice",
                        example = "Rest and hydration"
                )
        )
        List<String> recommendations
) {
}