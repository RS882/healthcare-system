package com.healthcare.aiservice.security.service;

import com.healthcare.aiservice.exception.UserAuthInfoNotFoundException;
import com.healthcare.aiservice.exception.UserServiceUnavailableException;

import com.healthcare.aiservice.security.constant.Role;
import com.healthcare.aiservice.security.dto.UserAuthInfoDto;
import com.healthcare.aiservice.security.feign_client.UserAuthInfoClient;
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Cached user authentication info service tests: ")
class CachedUserAuthInfoServiceTest {

    private static final long USER_ID = 42L;
    private static final long ANOTHER_USER_ID = 77L;

    @Mock
    private UserAuthInfoClient client;

    @InjectMocks
    private CachedUserAuthInfoService service;

    @Test
    void getUserAuthInfoByUserId_ShouldReturnAuthenticationInfo_WhenResponseIsValid() {
        UserAuthInfoDto expectedResponse = createAuthInfo(
                USER_ID,
                Set.of(
                        Role.ROLE_PATIENT,
                        Role.ROLE_ADMIN
                )
        );

        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(expectedResponse);

        UserAuthInfoDto result =
                service.getUserAuthInfoByUserId(USER_ID);

        assertThat(result)
                .isSameAs(expectedResponse);

        verify(client)
                .getUserAuthInfo(USER_ID);

        verifyNoMoreInteractions(client);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldRejectZeroUserId_WithoutCallingClient() {
        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(0L)
        )
                .isInstanceOf(UserAuthInfoNotFoundException.class)
                .hasMessage(
                        "Invalid user id for authentication lookup: 0"
                );

        verifyNoInteractions(client);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldRejectNegativeUserId_WithoutCallingClient() {
        long invalidUserId = -10L;

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(invalidUserId)
        )
                .isInstanceOf(UserAuthInfoNotFoundException.class)
                .hasMessage(
                        "Invalid user id for authentication lookup: -10"
                );

        verifyNoInteractions(client);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldRejectNullResponse() {
        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(null);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isInstanceOf(UserAuthInfoNotFoundException.class)
                .hasMessage(
                        "User service returned null authentication information for userId=42"
                );

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldRejectResponseWithoutUserId() {
        UserAuthInfoDto response = createAuthInfo(
                null,
                Set.of(Role.ROLE_PATIENT)
        );

        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(response);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isInstanceOf(UserAuthInfoNotFoundException.class)
                .hasMessage(
                        "User service returned authentication information without user id "
                                + "for requested userId=42"
                );

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldRejectMismatchedUserId() {
        UserAuthInfoDto response = createAuthInfo(
                ANOTHER_USER_ID,
                Set.of(Role.ROLE_PATIENT)
        );

        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(response);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isInstanceOf(UserAuthInfoNotFoundException.class)
                .hasMessage(
                        "User id mismatch. Requested userId=42, returned userId=77"
                );

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldRejectEmptyRoles() {
        UserAuthInfoDto response = createAuthInfo(
                USER_ID,
                Set.of()
        );

        when(client.getUserAuthInfo(USER_ID))
                .thenReturn(response);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isInstanceOf(UserAuthInfoNotFoundException.class)
                .hasMessage(
                        "User service returned empty roles for userId=42"
                );

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldRethrowUserAuthInfoNotFoundException() {
        UserAuthInfoNotFoundException expectedException =
                new UserAuthInfoNotFoundException(
                        "User was not found by user-service"
                );

        when(client.getUserAuthInfo(USER_ID))
                .thenThrow(expectedException);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isSameAs(expectedException);

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldRethrowUserServiceUnavailableException() {
        UserServiceUnavailableException expectedException =
                new UserServiceUnavailableException(
                        "User service is unavailable"
                );

        when(client.getUserAuthInfo(USER_ID))
                .thenThrow(expectedException);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isSameAs(expectedException);

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldRethrowOtherAuthenticationException() {
        BadCredentialsException expectedException =
                new BadCredentialsException(
                        "Authentication data is invalid"
                );

        when(client.getUserAuthInfo(USER_ID))
                .thenThrow(expectedException);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isSameAs(expectedException);

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldWrapRetryableException() {
        RetryableException retryableException =
                mock(RetryableException.class);

        when(client.getUserAuthInfo(USER_ID))
                .thenThrow(retryableException);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isInstanceOf(UserServiceUnavailableException.class)
                .hasMessage(
                        "Timeout while requesting authentication information from user-service."
                )
                .hasCause(retryableException);

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldWrapUnexpectedFeignException() {
        FeignException feignException =
                mock(FeignException.class);

        when(client.getUserAuthInfo(USER_ID))
                .thenThrow(feignException);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isInstanceOf(UserServiceUnavailableException.class)
                .hasMessage(
                        "Unexpected Feign error while requesting "
                                + "authentication information from user-service."
                )
                .hasCause(feignException);

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    @Test
    void getUserAuthInfoByUserId_ShouldWrapUnexpectedRuntimeException() {
        RuntimeException runtimeException =
                new RuntimeException("Unexpected failure");

        when(client.getUserAuthInfo(USER_ID))
                .thenThrow(runtimeException);

        assertThatThrownBy(
                () -> service.getUserAuthInfoByUserId(USER_ID)
        )
                .isInstanceOf(UserServiceUnavailableException.class)
                .hasMessage(
                        "Failed to retrieve authentication information from user-service."
                )
                .hasCause(runtimeException);

        verify(client)
                .getUserAuthInfo(USER_ID);
    }

    private UserAuthInfoDto createAuthInfo(
            Long userId,
            Set<Role> roles
    ) {
        return new UserAuthInfoDto(
                userId,
                roles
        );
    }
}