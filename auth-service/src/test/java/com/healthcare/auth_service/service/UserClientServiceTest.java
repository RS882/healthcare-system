package com.healthcare.auth_service.service;

import com.healthcare.auth_service.domain.AuthUserDetails;
import com.healthcare.auth_service.domain.dto.UserAuthDto;
import com.healthcare.auth_service.domain.dto.UserLookupDto;
import com.healthcare.auth_service.exception_handler.exception.AccessDeniedException;
import com.healthcare.auth_service.exception_handler.exception.ServiceUnavailableException;
import com.healthcare.auth_service.service.feignClient.UserClient;
import com.healthcare.auth_service.validator.UserInfoDtoValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private UserAuthDto activeUser;
    private UserAuthDto inactiveUser;

    @BeforeEach
    void setUp() {

        activeUser = UserAuthDto.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(PASSWORD)
                .roles(Set.of(USER_ROLE))
                .enabled(true)
                .build();

        inactiveUser = UserAuthDto.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(PASSWORD)
                .roles(Set.of(USER_ROLE))
                .enabled(false)
                .build();
    }

    @Nested
    @DisplayName("Get user by email tests")
    public class GetUserByEmailTests {
        @Test
        void positive_should_return_authUserDetails_when_active() {

            when(userClient.lookupUser(new UserLookupDto(EMAIL)))
                    .thenReturn(activeUser);

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

            when(userClient.lookupUser(new UserLookupDto(EMAIL)))
                    .thenReturn(inactiveUser);

            assertThrows(AccessDeniedException.class, () -> service.getUserByEmail(EMAIL));
        }

        @Test
        void negative_should_throw_UserNotFoundException_on_error() {

            when(userClient.lookupUser(new UserLookupDto(EMAIL)))
                    .thenThrow(new RuntimeException("Unexpected"));

            assertThrows(ServiceUnavailableException.class, () -> service.getUserByEmail(EMAIL));
        }
    }
}
