package com.healthcare.aiservice.common.provider.logging;

import com.healthcare.aiservice.config.constant.FeatureName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention( RetentionPolicy.RUNTIME)
public @interface LogAiUsage {

    FeatureName feature();
}
