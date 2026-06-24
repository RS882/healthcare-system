package com.healthcare.aiservice.repository.projection;

import com.healthcare.aiservice.config.constant.FeatureName;

public interface FeatureCountProjection {

    FeatureName getFeature();

    long getCount();
}