package com.healthcare.aiservice.common.prompt.logging;

import com.healthcare.aiservice.common.prompt.model.ResolvedPrompt;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AiPromptResolutionLoggingAspect {

    @AfterReturning(
            pointcut = "execution(* com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptResolver.resolvePrompt(..))",
            returning = "resolvedPrompt"
    )
    public void logResolvedPrompt(ResolvedPrompt resolvedPrompt) {

        if (resolvedPrompt == null || resolvedPrompt.key() == null) {
            return;
        }
        if (resolvedPrompt.version() == null) {
            log.info(
                    "AI prompt resolved. source={}, feature={}, type={}, targetModel={}",
                    resolvedPrompt.source(),
                    resolvedPrompt.key().feature(),
                    resolvedPrompt.key().type(),
                    resolvedPrompt.key().targetModel());
        } else {
            log.info(
                    "AI prompt resolved. source={}, feature={}, type={}, targetModel={}, version={}",
                    resolvedPrompt.source(),
                    resolvedPrompt.key().feature(),
                    resolvedPrompt.key().type(),
                    resolvedPrompt.key().targetModel(),
                    resolvedPrompt.version()
            );
        }
    }
}