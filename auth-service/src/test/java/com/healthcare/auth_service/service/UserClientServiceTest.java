package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.RegistrationDto;
import com.healthcare.auth_service.domain.dto.UserInfoDto;
import com.healthcare.auth_service.exception_handler.exception.AccessDeniedException;
import com.healthcare.auth_service.exception_handler.exception.ServiceUnavailableException;
import com.healthcare.auth_service.exception_handler.exception.UserNotFoundException;
import com.healthcare.auth_service.service.feignClient.UserClient;
import com.healthcare.auth_service.validator.UserInfoDtoValidator;
import feign.FeignException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class UserClientServiceTest {

    @InjectMocks
    private UserClientServiceImpl service;

    @Mock
    private UserClient userClient;

    @Mock
    private UserInfoDtoValidator validator;

    private final String EMAIL = "test@example.com";
    private final Long USER_ID = 1L;
    private final String USER_ROLE = "ROLE_TEST";
    private final String PASSWORD = "password";

    private UserInfoDto activeUser;
    private UserInfoDto inactiveUser;
    private RegistrationDto regDto;

    @BeforeEach
    void setUp() {

        activeUser = UserInfoDto.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(PASSWORD)
                .roles(Set.of(USER_ROLE))
                .enabled(true)
                .build();

        inactiveUser = UserInfoDto.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(PASSWORD)
                .roles(Set.of(USER_ROLE))
                .enabled(false)
                .build();

        regDto = RegistrationDto.builder()
                .userEmail(EMAIL)
                .userName("Test User")
                .password(PASSWORD)
                .build();
    }

    @Nested
    @DisplayName("Get user by email tests")
    public class GetUserByEmailTests {
        @Test
        void positive_should_return_authUserDetails_when_active() {
            when(userClient.getUserByEmail(EMAIL)).thenReturn(activeUser);

            AuthUserDetails result = service.getUserByEmail(EMAIL);

            assertNotNull(result);
            assertEquals(EMAIL, result.getUsername());
            assertEquals(USER_ID, result.getId());
            assertEquals(PASSWORD, result.getPassword());
            Set<String> actualRoles = result.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            assertEquals(Set.of(USER_ROLE), actualRoles);
            assertTrue(result.isEnabled());
            verify(validator).validateUser(activeUser);
        }

        @Test
        void negative_should_throw_when_inactive() {
            when(userClient.getUserByEmail(EMAIL)).thenReturn(inactiveUser);

            assertThrows(AccessDeniedException.class, () -> service.getUserByEmail(EMAIL));
        }

        @Test
        void negative_should_throw_UserNotFoundException_on_error() {
            when(userClient.getUserByEmail(EMAIL)).thenThrow(new RuntimeException("Unexpected"));

            assertThrows(UserNotFoundException.class, () -> service.getUserByEmail(EMAIL));
        }
    }

    @Nested
    @DisplayName("Register user")
    public class RegisterUserTests {

        @Test
        void positive_should_return_authUserDetails_when_ok() {
            when(userClient.registerUser(regDto)).thenReturn(activeUser);

            AuthUserDetails result = service.registerUser(regDto);

            assertNotNull(result);
            assertEquals(EMAIL, result.getUsername());
            assertEquals(USER_ID, result.getId());
            assertEquals(PASSWORD, result.getPassword());
            Set<String> actualRoles = result.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            assertEquals(Set.of(USER_ROLE), actualRoles);
            assertTrue(result.isEnabled());
            verify(validator).validateUser(activeUser);
        }

        @Test
        void negative_should_throw_UserNotFoundException_when_null_returned() {
            when(userClient.registerUser(regDto)).thenReturn(null);

            assertThrows(UserNotFoundException.class, () -> service.registerUser(regDto));
        }

        @Test
        void negative_should_throw_UserNotFoundException_when_feign_not_found() {
            when(userClient.registerUser(regDto)).thenThrow(mock(FeignException.NotFound.class));

            assertThrows(UserNotFoundException.class, () -> service.registerUser(regDto));
        }

        @Test
        void negative_should_throw_ServiceUnavailableException_on_other_error() {
            when(userClient.registerUser(regDto)).thenThrow(new RuntimeException("Connection timeout"));

            assertThrows(ServiceUnavailableException.class, () -> service.registerUser(regDto));
        }
    }
}
