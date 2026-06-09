package com.healthcare.aiservice.common.provider.logging.annotation;

import com.healthcare.aiservice.config.constant.FeatureName;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention( RetentionPolicy.RUNTIME)
@Documented
public @interface LogAiUsage {

    FeatureName feature();
}
