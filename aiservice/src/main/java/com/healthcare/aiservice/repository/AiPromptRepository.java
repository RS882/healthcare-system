package com.healthcare.aiservice.repository;


import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.config.constant.AiProviderModel;
import com.healthcare.aiservice.config.constant.PromptType;
import com.healthcare.aiservice.config.constant.FeatureName;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AiPromptRepository extends MongoRepository<AiPrompt, String> {

    Optional<AiPrompt> findTopByFeatureAndTypeAndTargetModelOrderByVersionDesc(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    );

    List<AiPrompt> findByFeatureAndTypeAndTargetModelOrderByVersionDesc(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    );

    Optional<AiPrompt> findByFeatureAndTypeAndTargetModelAndActiveTrue(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    );

    List<AiPrompt> findAllByFeatureAndTypeAndTargetModelAndActiveTrue(
            FeatureName feature,
            PromptType type,
            AiProviderModel targetModel
    );
}
