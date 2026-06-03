package com.healthcare.aiservice.common.provider.logging;

import com.healthcare.aiservice.config.propertie.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AiUsageLoggingAspect {

    private final AiProperties aiProperties;

    @Around("@annotation(logAiUsage)")
    public Object logAiUsage(
            ProceedingJoinPoint joinPoint,
            LogAiUsage logAiUsage) throws Throwable {

        long startTime = System.currentTimeMillis();
        String feature =  logAiUsage.feature().getValue();
        String provider =aiProperties.provider();
        String model = aiProperties.model();

        try {

            Object result = joinPoint.proceed();

            log.info(
                    "AI request completed successfully. feature={}, provider={}, model={}, durationMs={}",
                    feature,
                    provider,
                    model,
                    System.currentTimeMillis() - startTime
            );
            return result;

        } catch (Exception ex) {
            log.error(
                    "AI request failed. feature={}, provider={}, model={}, durationMs={}, error={}",
                    feature,
                    provider,
                    model,
                    System.currentTimeMillis() - startTime,
                    ex.getClass().getSimpleName(),
                    ex
            );
            throw ex;
        }
    }
}
