package com.healthcare.aiservice.config.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeatureName {

    MEDICAL_SUMMARY("medical-summary"),
    MEDICAL_EXTRACTION("medical-extraction"),
    MESSAGE_CLASSIFICATION("message-classification");

    private final String value;
}
