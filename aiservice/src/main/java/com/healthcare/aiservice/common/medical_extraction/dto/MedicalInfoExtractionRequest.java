package com.healthcare.aiservice.common.medical_extraction.dto;

import com.healthcare.aiservice.common.dto.NoteBasedRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import static com.healthcare.aiservice.common.dto.SwaggerDescriptions.NOTE_DESCRIPTION;

@Schema(
        name = "MedicalInfoExtractionRequest",
        description = "Request for extracting structured medical information from a free-text medical note"
)
public record MedicalInfoExtractionRequest(

        @NotBlank(message = "Medical note must not be blank")
        @Schema(
                description = """
                        Medical note text to analyze
                        """ + NOTE_DESCRIPTION,
                example = """
                        Patient reports fever, headache and dry cough for 5 days.\\n\
                        Diagnosed with viral upper respiratory infection.\\n\
                        Prescribed Paracetamol 500mg.\\n\
                        Recommended rest and hydration.
                         """
        )
        String note
) implements NoteBasedRequest {
}