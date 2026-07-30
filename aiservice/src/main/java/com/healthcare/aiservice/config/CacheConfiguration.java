package com.healthcare.aiservice.config;

import com.healthcare.aiservice.common.prompt.cache.CacheNames;
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
public class CacheConfiguration {

    private static final Duration DEFAULT_CACHE_TTL =
            Duration.ofMinutes(30);

    private static final Duration ACTIVE_PROMPTS_CACHE_TTL =
            Duration.ofHours(1);

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory
    ) {
        RedisCacheConfiguration defaultConfiguration =
                createCacheConfiguration(DEFAULT_CACHE_TTL);

        RedisCacheConfiguration activePromptsConfiguration =
                createCacheConfiguration(ACTIVE_PROMPTS_CACHE_TTL);

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

    private RedisCacheConfiguration createCacheConfiguration(
            Duration ttl
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
                                .fromSerializer(
                                        new GenericJackson2JsonRedisSerializer()
                                )
                );
    }
}