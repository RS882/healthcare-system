package com.healthcare.aiservice.common.provider.logging;

import com.healthcare.aiservice.common.provider.logging.annotation.LogAiUsage;
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
    private final AiUsageLogger aiUsageLogger;

    @Around("@annotation(logAiUsage)")
    public Object logAiUsage(
            ProceedingJoinPoint joinPoint,
            LogAiUsage logAiUsage
    ) throws Throwable {

        long startTime = System.currentTimeMillis();

        var feature = logAiUsage.feature();
        String provider = aiProperties.provider();
        String model = aiProperties.model();

        Object request = extractRequest(joinPoint);

        try {
            Object response = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - startTime;

            aiUsageLogger.logSuccess(
                    feature,
                    provider,
                    model,
                    request,
                    response,
                    durationMs
            );

            log.info(
                    "AI request completed successfully. feature={}, provider={}, model={}, durationMs={}",
                    feature.getValue(),
                    provider,
                    model,
                    durationMs
            );

            return response;

        } catch (Exception ex) {
            long durationMs = System.currentTimeMillis() - startTime;

            aiUsageLogger.logFailure(
                    feature,
                    provider,
                    model,
                    request,
                    ex,
                    durationMs
            );

            log.error(
                    "AI request failed. feature={}, provider={}, model={}, durationMs={}, error={}",
                    feature.getValue(),
                    provider,
                    model,
                    durationMs,
                    ex.getClass().getSimpleName(),
                    ex
            );

            throw ex;
        }
    }

    private Object extractRequest(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        if (args == null || args.length == 0) {
            return null;
        }

        return args[0];
    }
}