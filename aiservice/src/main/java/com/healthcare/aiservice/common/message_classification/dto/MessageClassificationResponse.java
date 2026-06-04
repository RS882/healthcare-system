package com.healthcare.aiservice.common.message_classification.dto;

import com.healthcare.aiservice.common.message_classification.category.MessageCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI-generated patient message classification response")
public record MessageClassificationResponse(

        @Schema(
                description = "Detected message category",
                example = "APPOINTMENT"
        )
        MessageCategory category,

        @Schema(
                description = "Short reason for the selected category",
                example = "Patient wants to reschedule an appointment."
        )
        String reason

) {
}
