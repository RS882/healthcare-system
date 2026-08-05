package com.healthcare.aiservice.security.service;


import com.healthcare.aiservice.cache.CacheNames;
import com.healthcare.aiservice.exception.UserAuthInfoNotFoundException;
import com.healthcare.aiservice.exception.UserServiceUnavailableException;
import com.healthcare.aiservice.security.dto.UserAuthInfoDto;
import com.healthcare.aiservice.security.feign_client.UserAuthInfoClient;
import com.healthcare.aiservice.security.service.interfacies.UserAuthInfoService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class CachedUserAuthInfoService
        implements UserAuthInfoService {

    private final UserAuthInfoClient client;

    @Override
    @Cacheable(
            cacheNames = CacheNames.USER_AUTH_INFO,
            key = "#userId"
    )
    public UserAuthInfoDto getUserAuthInfoByUserId(
            long userId
    ) {
        log.debug(
                "Authentication information not found in cache. Loading from user-service. userId={}",
                userId
        );

        validateUserId(userId);

        UserAuthInfoDto authInfo =
                loadFromUserService(userId);

        validateResponse(
                authInfo,
                userId
        );

        log.debug(
                "Authentication information successfully loaded for userId={}.",
                userId
        );

        return authInfo;
    }

    @CacheEvict(
            cacheNames = CacheNames.USER_AUTH_INFO,
            key = "#userId"
    )
    public void evictUserAuthInfo(
            long userId
    ) {
        // No implementation required.
        // Spring Cache removes the entry after successful method execution.
    }

    private UserAuthInfoDto loadFromUserService(
            long userId
    ) {
        try {

            log.debug(
                    "Authentication information not found in cache. Loading from user-service. userId={}.",
                    userId
            );
            long started = System.nanoTime();

            UserAuthInfoDto userAuthInfoDto = client.getUserAuthInfo(userId);

            log.debug(
                    "Authentication information received from user-service. userId={}, duration={} ms.",
                    userId,
                    Duration.ofNanos(System.nanoTime() - started).toMillis()
            );

            return userAuthInfoDto;

        } catch (AuthenticationException exception) {
            throw exception;

        } catch (RetryableException exception) {
            throw new UserServiceUnavailableException(
                    "Timeout while requesting authentication information from user-service.",
                    exception
            );

        } catch (FeignException exception) {
            // Unexpected Feign failure not handled by ErrorDecoder.
            throw new UserServiceUnavailableException(
                    "Unexpected Feign error while requesting authentication information from user-service.",
                    exception
            );

        } catch (RuntimeException exception) {
            throw new UserServiceUnavailableException(
                    "Failed to retrieve authentication information from user-service.",
                    exception
            );
        }
    }

    private void validateUserId(
            long userId
    ) {
        if (userId <= 0) {
            throw new UserAuthInfoNotFoundException(
                    "Invalid user id for authentication lookup: " + userId
            );
        }
    }

    private void validateResponse(
            UserAuthInfoDto authInfo,
            long expectedUserId
    ) {
        if (authInfo == null) {
            throw new UserAuthInfoNotFoundException(
                    "User service returned null authentication information for userId="
                            + expectedUserId
            );
        }

        if (authInfo.userId() == null) {
            throw new UserAuthInfoNotFoundException(
                    "User service returned authentication information without user id for requested userId="
                            + expectedUserId
            );
        }

        if (!authInfo.userId().equals(expectedUserId)) {
            throw new UserAuthInfoNotFoundException(
                    "User id mismatch. Requested userId="
                            + expectedUserId
                            + ", returned userId="
                            + authInfo.userId()
            );
        }

        if (authInfo.roles() == null
                || authInfo.roles().isEmpty()) {
            throw new UserAuthInfoNotFoundException(
                    "User service returned empty roles for userId="
                            + expectedUserId
            );
        }
    }
}