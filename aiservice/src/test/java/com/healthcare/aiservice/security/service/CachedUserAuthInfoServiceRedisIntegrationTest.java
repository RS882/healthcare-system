package com.healthcare.aiservice.security.service;


import com.healthcare.aiservice.cache.CacheNames;
import com.healthcare.aiservice.config.AbstractMongoRedisIntegrationTest;
import com.healthcare.aiservice.exception.UserServiceUnavailableException;

import com.healthcare.aiservice.security.constant.Role;
import com.healthcare.aiservice.security.dto.UserAuthInfoDto;
import com.healthcare.aiservice.security.feign_client.UserAuthInfoClient;
import com.healthcare.aiservice.security.service.interfacies.UserAuthInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Cached user authentication info Redis integration tests: ")
class CachedUserAuthInfoServiceRedisIntegrationTest
        extends AbstractMongoRedisIntegrationTest {

    private static final long USER_ID = 42L;

    @Autowired
    private UserAuthInfoService service;

    @Autowired
    private CachedUserAuthInfoService cachedUserAuthInfoService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private UserAuthInfoClient client;

    @BeforeEach
    void setUp() {
        Cache cache = getUserAuthInfoCache();

        cache.clear();

        reset(client);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldStoreAndReadAuthenticationInfoFromRedis() {
        UserAuthInfoDto expected = createUserAuthInfo();

        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(expected);

        UserAuthInfoDto firstResult =
                service.getUserAuthInfoByUserId(USER_ID);

        UserAuthInfoDto secondResult =
                service.getUserAuthInfoByUserId(USER_ID);

        assertThat(cacheManager)
                .isInstanceOf(RedisCacheManager.class);

        assertThat(firstResult)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        assertThat(secondResult)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        verify(client, times(1))
                .getUserAuthInfo(USER_ID);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isTrue();
    }

    @Test
    void getUserAuthInfoByUserId_ShouldUseRedis_WhenFeignClientFailsAfterInitialLoad() {
        UserAuthInfoDto expected = createUserAuthInfo();

        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(expected);

        UserAuthInfoDto firstResult =
                service.getUserAuthInfoByUserId(USER_ID);

        when(client.getUserAuthInfo(USER_ID))
                .thenThrow(
                        new UserServiceUnavailableException(
                                "User service is unavailable"
                        )
                );

        UserAuthInfoDto secondResult =
                service.getUserAuthInfoByUserId(USER_ID);

        assertThat(firstResult)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        assertThat(secondResult)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        verify(client, times(1))
                .getUserAuthInfo(USER_ID);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isTrue();
    }

    @Test
    void getUserAuthInfoByUserId_ShouldSetConfiguredTtlForCachedAuthenticationInfo() {
        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(createUserAuthInfo());

        service.getUserAuthInfoByUserId(USER_ID);

        Long ttlSeconds = redisTemplate.getExpire(
                redisKey(),
                TimeUnit.SECONDS
        );

        assertThat(ttlSeconds)
                .isNotNull()
                .isPositive()
                .isLessThanOrEqualTo(30L);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldNotCacheException() {
        UserServiceUnavailableException failure =
                new UserServiceUnavailableException(
                        "User service is unavailable"
                );

        when(client.getUserAuthInfo(USER_ID))
                .thenThrow(failure);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isSameAs(failure);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isSameAs(failure);

        verify(client, times(2))
                .getUserAuthInfo(USER_ID);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isFalse();
    }

    @Test
    void getUserAuthInfoByUserId_ShouldLoadFromFeignAgain_AfterCacheEntryExpires()
            throws InterruptedException {

        UserAuthInfoDto expected = createUserAuthInfo();

        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(expected);

        service.getUserAuthInfoByUserId(USER_ID);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isTrue();

        Boolean expirationSet = redisTemplate.expire(
                redisKey(),
                100,
                TimeUnit.MILLISECONDS
        );

        assertThat(expirationSet)
                .isTrue();

        Thread.sleep(200);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isFalse();

        UserAuthInfoDto result =
                service.getUserAuthInfoByUserId(USER_ID);

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        verify(client, times(2))
                .getUserAuthInfo(USER_ID);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isTrue();
    }

    @Test
    void evictUserAuthInfo_ShouldRemoveCachedAuthenticationInfo_AndForceReload() {
        UserAuthInfoDto firstResponse =
                new UserAuthInfoDto(
                        USER_ID,
                        Set.of(Role.ROLE_PATIENT)
                );

        UserAuthInfoDto updatedResponse =
                new UserAuthInfoDto(
                        USER_ID,
                        Set.of(
                                Role.ROLE_PATIENT,
                                Role.ROLE_ADMIN
                        )
                );

        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(firstResponse)
                .thenReturn(updatedResponse);

        UserAuthInfoDto firstResult =
                service.getUserAuthInfoByUserId(USER_ID);

        assertThat(firstResult)
                .usingRecursiveComparison()
                .isEqualTo(firstResponse);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isTrue();

        verify(client, times(1))
                .getUserAuthInfo(USER_ID);

        cachedUserAuthInfoService.evictUserAuthInfo(USER_ID);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isFalse();

        UserAuthInfoDto secondResult =
                service.getUserAuthInfoByUserId(USER_ID);

        assertThat(secondResult)
                .usingRecursiveComparison()
                .isEqualTo(updatedResponse);

        verify(client, times(2))
                .getUserAuthInfo(USER_ID);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isTrue();
    }

    @Test
    void evictUserAuthInfo_ShouldComplete_WhenCacheEntryDoesNotExist() {
        assertThat(redisTemplate.hasKey(redisKey()))
                .isFalse();

        cachedUserAuthInfoService.evictUserAuthInfo(USER_ID);

        assertThat(redisTemplate.hasKey(redisKey()))
                .isFalse();

        verifyNoInteractions(client);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldReturnValidResult_WhenCalledConcurrently()
            throws Exception {

        UserAuthInfoDto expected = createUserAuthInfo();

        when(client.getUserAuthInfo(USER_ID))
                .thenAnswer(invocation -> {
                    Thread.sleep(100);
                    return expected;
                });

        int threadCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch ready =
                new CountDownLatch(threadCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<UserAuthInfoDto>> futures =
                new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {
                futures.add(
                        executor.submit(() -> {
                            ready.countDown();
                            start.await();

                            return service.getUserAuthInfoByUserId(USER_ID);
                        })
                );
            }

            assertThat(ready.await(5, TimeUnit.SECONDS))
                    .isTrue();

            start.countDown();

            for (Future<UserAuthInfoDto> future : futures) {
                assertThat(
                        future.get(5, TimeUnit.SECONDS)
                )
                        .usingRecursiveComparison()
                        .isEqualTo(expected);
            }

            assertThat(redisTemplate.hasKey(redisKey()))
                    .isTrue();

        } finally {
            executor.shutdownNow();
        }
    }

    private Cache getUserAuthInfoCache() {
        Cache cache = cacheManager.getCache(
                CacheNames.USER_AUTH_INFO
        );

        assertThat(cache)
                .as("User authentication info cache must be configured")
                .isNotNull();

        return cache;
    }

    private String redisKey() {
        return CacheNames.USER_AUTH_INFO
                + "::"
                + USER_ID;
    }

    private UserAuthInfoDto createUserAuthInfo() {
        return new UserAuthInfoDto(
                USER_ID,
                Set.of(
                        Role.ROLE_PATIENT,
                        Role.ROLE_ADMIN
                )
        );
    }
}
