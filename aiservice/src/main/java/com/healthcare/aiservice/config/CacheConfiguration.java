package com.healthcare.aiservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

import com.healthcare.aiservice.cache.CacheNames;
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
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfiguration {

    private final CacheProperties properties;

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        ObjectMapper cacheObjectMapper =
                objectMapper.copy();

        cacheObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(
                        cacheObjectMapper
                );

        RedisCacheConfiguration defaultConfiguration =
                createCacheConfiguration(
                        properties.defaultTtl(),
                        valueSerializer
                );

        RedisCacheConfiguration activePromptsConfiguration =
                createCacheConfiguration(
                        properties.activePrompt().ttl(),
                        valueSerializer
                );

        RedisCacheConfiguration userAuthInfoConfiguration =
                createCacheConfiguration(
                        properties.userAuthInfo().ttl(),
                        valueSerializer
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(
                        Map.of(
                                CacheNames.ACTIVE_PROMPTS,
                                activePromptsConfiguration,

                                CacheNames.USER_AUTH_INFO,
                                userAuthInfoConfiguration
                        )
                )
                .transactionAware()
                .build();
    }

    private RedisCacheConfiguration createCacheConfiguration(
            Duration ttl,
            GenericJackson2JsonRedisSerializer valueSerializer
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