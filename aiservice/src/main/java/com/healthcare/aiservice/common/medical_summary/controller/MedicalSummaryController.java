package com.healthcare.aiservice.common.medical_summary.controller;


import com.healthcare.aiservice.common.medical_summary.controller.API.MedicalSummaryAPI;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryRequest;
import com.healthcare.aiservice.common.medical_summary.dto.MedicalSummaryResponse;
import com.healthcare.aiservice.common.medical_summary.service.MedicalSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MedicalSummaryController implements MedicalSummaryAPI {

    private final MedicalSummaryService medicalSummaryService;

    @Override
    public ResponseEntity<MedicalSummaryResponse> summarize(
            MedicalSummaryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(medicalSummaryService.summarize(request));
    }
}
