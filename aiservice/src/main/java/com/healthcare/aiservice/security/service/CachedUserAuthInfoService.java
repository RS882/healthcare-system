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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CachedUserAuthInfoService
        implements UserAuthInfoService {

    private final UserAuthInfoClient client;

    @Override
    @Cacheable(
            cacheNames = CacheNames.USER_AUTH_INFO,
            key = "#userId",
            unless = "#result == null",
            sync = true
    )
    public UserAuthInfoDto getUserAuthInfoByUserId(
            long userId
    ) {
        validateUserId(userId);

        UserAuthInfoDto authInfo =
                loadFromUserService(userId);

        validateResponse(
                authInfo,
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
            return client.getUserAuthInfo(userId);

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