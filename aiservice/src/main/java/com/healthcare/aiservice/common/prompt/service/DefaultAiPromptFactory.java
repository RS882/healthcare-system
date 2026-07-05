package com.healthcare.aiservice.common.prompt.service;

import com.healthcare.aiservice.common.dto.NoteBasedRequest;
import com.healthcare.aiservice.common.prompt.model.AiPromptKey;
import com.healthcare.aiservice.common.prompt.model.AiProviderModel;
import com.healthcare.aiservice.common.prompt.model.PromptType;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptFactory;
import com.healthcare.aiservice.common.prompt.service.interfaces.AiPromptResolver;
import com.healthcare.aiservice.common.prompt.service.interfaces.PromptProvider;
import com.healthcare.aiservice.config.constant.FeatureName;
import com.healthcare.aiservice.exception.AiPromptProviderNotFoundException;
import com.healthcare.aiservice.exception.InvalidRequestTypeForFeatureException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class DefaultAiPromptFactory implements AiPromptFactory {

    private static final AiProviderModel DEFAULT_MODEL = AiProviderModel.LLAMA_3;

    private final Map<FeatureName, PromptProvider<? extends NoteBasedRequest>> providersByFeature;
    private final AiPromptResolver promptResolver;

    public DefaultAiPromptFactory(
            List<PromptProvider<? extends NoteBasedRequest>> providers,
            AiPromptResolver promptResolver
    ) {
        this.providersByFeature = providers.stream()
                .collect(Collectors.toMap(PromptProvider::feature, Function.identity()));
        this.promptResolver = promptResolver;
    }

    @Override
    public String getSystemPrompt(FeatureName feature) {
        PromptProvider<? extends NoteBasedRequest> provider = getProvider(feature);

        return promptResolver.resolvePrompt(
                buildKey(feature, PromptType.SYSTEM),
                provider::systemPrompt
        );
    }

    @Override
    public String getUserPrompt(FeatureName feature, NoteBasedRequest request) {
        PromptProvider<? extends NoteBasedRequest> provider = getProvider(feature);

        return promptResolver.resolvePrompt(
                buildKey(feature, PromptType.USER),
                () -> buildUserPrompt(provider, request)
        );
    }

    private <T extends NoteBasedRequest> String buildUserPrompt(
            PromptProvider<T> provider,
            NoteBasedRequest request
    ) {
        if (!provider.requestType().isInstance(request)) {
            throw new InvalidRequestTypeForFeatureException(provider, request.getClass());
        }

        T typedRequest = provider.requestType().cast(request);

        return provider.userPrompt(typedRequest);
    }

    private PromptProvider<? extends NoteBasedRequest> getProvider(FeatureName feature) {
        PromptProvider<? extends NoteBasedRequest> provider = providersByFeature.get(feature);

        if (provider == null) {
            throw new AiPromptProviderNotFoundException(feature);
        }

        return provider;
    }

    private AiPromptKey buildKey(FeatureName feature, PromptType type) {
        return AiPromptKey.builder()
                .feature(feature)
                .type(type)
                .targetModel(DEFAULT_MODEL)
                .build();
    }
}