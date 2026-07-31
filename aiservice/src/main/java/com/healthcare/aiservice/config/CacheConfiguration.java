package com.healthcare.aiservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.common.prompt.cache.CacheNames;
import com.healthcare.aiservice.common.prompt.model.AiPrompt;
import com.healthcare.aiservice.config.propertie.cache_propertie.CacheProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfiguration {

    private final CacheProperties props;
    private final ObjectMapper objectMapper;

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory
    ) {
        RedisCacheConfiguration defaultConfiguration =
                createDefaultCacheConfiguration(
                        props.defaultTtl()
                );

        RedisCacheConfiguration activePromptsConfiguration =
                createActivePromptsCacheConfiguration(
                        props.activePrompt().ttl()
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(
                        Map.of(
                                CacheNames.ACTIVE_PROMPTS,
                                activePromptsConfiguration
                        )
                )
                .transactionAware()
                .build();
    }

    private RedisCacheConfiguration createDefaultCacheConfiguration(
            Duration ttl
    ) {
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(
                        objectMapper.copy()
                );

        return createCacheConfiguration(
                ttl,
                valueSerializer
        );
    }

    private RedisCacheConfiguration createActivePromptsCacheConfiguration(
            Duration ttl
    ) {
        Jackson2JsonRedisSerializer<AiPrompt> valueSerializer =
                new Jackson2JsonRedisSerializer<>(
                        objectMapper.copy(),
                        AiPrompt.class
                );

        return createCacheConfiguration(
                ttl,
                valueSerializer
        );
    }

    private RedisCacheConfiguration createCacheConfiguration(
            Duration ttl,
            RedisSerializer<?> valueSerializer
    ) {
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(
                                        new StringRedisSerializer()
                                )
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(valueSerializer)
                );
    }
}