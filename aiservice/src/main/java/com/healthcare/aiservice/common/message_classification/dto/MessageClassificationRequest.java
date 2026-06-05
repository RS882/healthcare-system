package com.healthcare.aiservice.common.message_classification.dto;

import com.healthcare.aiservice.common.dto.NoteBasedRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.healthcare.aiservice.common.dto.SwaggerDescriptions.NOTE_DESCRIPTION;

@Schema(description = "Request for patient message classification")
public record MessageClassificationRequest(

        @Schema(
                description = """
                        Patient message text
                        """ + NOTE_DESCRIPTION,
                example = "I need to reschedule my appointment for next week."
        )
        @NotBlank(message = "Note must not be blank")
        @Size(max = 5000, message = "Note is too long")
        String note

) implements NoteBasedRequest {
}