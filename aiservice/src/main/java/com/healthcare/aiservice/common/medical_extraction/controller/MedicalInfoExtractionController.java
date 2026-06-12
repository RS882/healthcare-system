package com.healthcare.aiservice.common.medical_extraction.controller;

import com.healthcare.aiservice.common.medical_extraction.controller.API.MedicalInfoExtractionAPI;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionRequest;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionResponse;
import com.healthcare.aiservice.common.medical_extraction.service.MedicalInfoExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MedicalInfoExtractionController implements MedicalInfoExtractionAPI {

    private final MedicalInfoExtractionService service;

    @Override
    public ResponseEntity<MedicalInfoExtractionResponse> extract(MedicalInfoExtractionRequest request) {
        return ResponseEntity.ok(service.extract(request));
    }
}
