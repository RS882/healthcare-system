package com.healthcare.aiservice.common.medical_summary.dto;

import com.healthcare.aiservice.common.dto.NoteBasedRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for medical note summarization")
public record MedicalSummaryRequest(

        @Schema(
                description = "Raw medical note text",
                example = """
                        Patient complains about headache and nausea.
                        Takes Ibuprofen daily.
                        Follow-up recommended in two weeks.
                        """
        )
        @NotBlank(message = "Medical note must not be blank")
        @Size(max = 12000, message = "Medical note is too long")
        String note

) implements NoteBasedRequest {
}
