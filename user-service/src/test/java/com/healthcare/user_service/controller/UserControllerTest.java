package com.healthcare.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.config.AbstractKafkaRedisMsqlTestContainer;
import com.healthcare.user_service.config.properties.HeaderRequestIdProperties;
import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.dto.ErrorResponse;
import com.healthcare.user_service.model.dto.auth.UserAuthDto;
import com.healthcare.user_service.model.dto.request.RegistrationDto;
import com.healthcare.user_service.model.dto.request.UserLookupDto;
import com.healthcare.user_service.model.dto.response.RegistrationResponse;
import com.healthcare.user_service.service.interfacies.RequestIdService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.healthcare.user_service.controller.API.ApiPaths.INTERNAL_LOOKUP_URL;
import static com.healthcare.user_service.controller.API.ApiPaths.REGISTRATION_URL;
import static com.healthcare.user_service.support.TestDataFactory.userEmail;
import static com.healthcare.user_service.support.TestDataFactory.userName;
import static com.healthcare.user_service.support.TestDataFactory.userPassword;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(UserControllerTest.TestSecurityConfig.class)
@DisplayName("Users controller integration tests: ")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@TestPropertySource(properties = {
        "security.config.enabled=false",
        "user-context-filter.enabled=false",
        "auth-filter.enabled=false",
        "internal-request-filter.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.kafka.listener.auto-startup=false"
})
class UserControllerTest
        extends AbstractKafkaRedisMsqlTestContainer {

    private static final String TEST_USER_NAME = userName();
    private static final String TEST_USER_EMAIL = userEmail();
    private static final String TEST_USER_PASSWORD = userPassword();

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HeaderRequestIdProperties headerRequestIdProps;

    @Autowired
    private RequestIdService requestIdService;

    private ErrorResponse checkErrorResponseResultWithoutCheckingValidationErrors(
            MvcResult result,
            HttpStatus status,
            String url
    ) throws Exception {

        String responseBody =
                result.getResponse().getContentAsString();

        ErrorResponse error =
                mapper.readValue(
                        responseBody,
                        ErrorResponse.class
                );

        assertNotNull(error.getMessage());

        assertEquals(
                status.value(),
                error.getStatus()
        );

        assertEquals(
                status.getReasonPhrase(),
                error.getError()
        );

        assertEquals(
                url,
                error.getPath()
        );

        return error;
    }

    private void checkErrorResponseResultWithValidationErrors(
            MvcResult result,
            HttpStatus status,
            String url
    ) throws Exception {

        ErrorResponse error =
                checkErrorResponseResultWithoutCheckingValidationErrors(
                        result,
                        status,
                        url
                );

        assertNotNull(error.getValidationErrors());

        assertFalse(
                error.getValidationErrors().isEmpty()
        );
    }

    private MvcResult regTestUser()
            throws Exception {

        return regTestUser(
                TEST_USER_EMAIL
        );
    }

    private MvcResult regTestUser(
            String email
    ) throws Exception {

        RegistrationDto dto =
                new RegistrationDto(
                        email,
                        TEST_USER_NAME,
                        TEST_USER_PASSWORD
                );

        String dtoJson =
                mapper.writeValueAsString(dto);

        UUID requestId =
                requestIdService.getRequestId();

        return mockMvc.perform(
                        post(REGISTRATION_URL)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .header(
                                        headerRequestIdProps.name(),
                                        requestId
                                )
                                .content(dtoJson)
                )
                .andExpect(
                        status().isCreated()
                )
                .andReturn();
    }

    @Nested
    @DisplayName("POST " + REGISTRATION_URL)
    class RegUserTest {

        private void checkErrorResponseResultWithValidationErrors(
                MvcResult result,
                HttpStatus status
        ) throws Exception {

            UserControllerTest.this
                    .checkErrorResponseResultWithValidationErrors(
                            result,
                            status,
                            REGISTRATION_URL
                    );
        }

        @Test
        void registration_user_should_return_201()
                throws Exception {

            String email =
                    TEST_USER_EMAIL + 0;

            MvcResult result =
                    regTestUser(email);

            String jsonResponse =
                    result.getResponse()
                            .getContentAsString();

            RegistrationResponse responseDto =
                    mapper.readValue(
                            jsonResponse,
                            RegistrationResponse.class
                    );

            Long id = responseDto.id();

            assertThat(id)
                    .isNotNull()
                    .isInstanceOf(Long.class);

            assertEquals(
                    email,
                    responseDto.email()
            );

            assertEquals(
                    TEST_USER_NAME,
                    responseDto.name()
            );
        }

        @ParameterizedTest(
                name = "Test {index}: registration data is incorrect [{arguments}]"
        )
        @MethodSource("incorrectLoginData")
        void registration_user_should_return_400_when_registration_data_is_wrong(
                RegistrationDto dto
        ) throws Exception {

            String dtoJson =
                    mapper.writeValueAsString(dto);

            UUID requestId =
                    requestIdService.getRequestId();

            MvcResult result =
                    mockMvc.perform(
                                    post(REGISTRATION_URL)
                                            .contentType(
                                                    MediaType.APPLICATION_JSON
                                            )
                                            .header(
                                                    headerRequestIdProps.name(),
                                                    requestId
                                            )
                                            .content(dtoJson)
                            )
                            .andExpect(
                                    status().isBadRequest()
                            )
                            .andReturn();

            checkErrorResponseResultWithValidationErrors(
                    result,
                    HttpStatus.BAD_REQUEST
            );
        }

        private static Stream<Arguments> incorrectLoginData() {

            return Stream.of(
                    Arguments.of(
                            new RegistrationDto(
                                    null,
                                    TEST_USER_NAME,
                                    TEST_USER_PASSWORD
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    "TEST_USER_EMAIL",
                                    TEST_USER_NAME,
                                    TEST_USER_PASSWORD
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    null,
                                    TEST_USER_PASSWORD
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    "   ",
                                    TEST_USER_PASSWORD
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    "tt",
                                    TEST_USER_PASSWORD
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    "skdfsdfjsldfjsldfaksjlaskahfkahsflkahsfkashflkahskfaskhfalksfaksflashfaskhlhfklashfsaklfhlkafshlsafhfaslhfka",
                                    TEST_USER_PASSWORD
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    TEST_USER_NAME,
                                    null
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    TEST_USER_NAME,
                                    "       "
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    TEST_USER_NAME,
                                    "1E"
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    TEST_USER_NAME,
                                    "skdfsdfjsldfjsldfaksjlaskahfkahsfkashflkahskfaskhfalksfaksflashfaskhlhfklashfsaklfhlkafshlsafhfaslhfka"
                            )
                    ),

                    Arguments.of(
                            new RegistrationDto(
                                    "TEST_USER_EMAIL",
                                    "   ",
                                    "1E"
                            )
                    )
            );
        }
    }

    @Nested
    @DisplayName("POST " + INTERNAL_LOOKUP_URL)
    class UserByEmailTest {

        @Test
        void get_user_auth_should_return_200()
                throws Exception {

            regTestUser();

            UserLookupDto dto =
                    new UserLookupDto(
                            TEST_USER_EMAIL
                    );

            String dtoJson =
                    mapper.writeValueAsString(dto);

            UUID requestId =
                    requestIdService.getRequestId();

            MvcResult result =
                    mockMvc.perform(
                                    post(INTERNAL_LOOKUP_URL)
                                            .contentType(
                                                    MediaType.APPLICATION_JSON
                                            )
                                            .header(
                                                    headerRequestIdProps.name(),
                                                    requestId
                                            )
                                            .content(dtoJson)
                            )
                            .andExpect(
                                    status().isOk()
                            )
                            .andReturn();

            String jsonResponse =
                    result.getResponse()
                            .getContentAsString();

            UserAuthDto responseDto =
                    mapper.readValue(
                            jsonResponse,
                            UserAuthDto.class
                    );

            Long id =
                    responseDto.id();

            assertThat(id)
                    .isNotNull()
                    .isInstanceOf(Long.class);

            String password =
                    responseDto.password();

            assertThat(password)
                    .isNotNull()
                    .isInstanceOf(String.class);

            assertEquals(
                    TEST_USER_EMAIL,
                    responseDto.email()
            );

            Optional<String> firstRole =
                    responseDto.roles()
                            .stream()
                            .findFirst();

            assertTrue(
                    firstRole.isPresent()
            );

            assertEquals(
                    Role.ROLE_PATIENT.name(),
                    firstRole.get()
            );

            assertTrue(
                    responseDto.enabled()
            );
        }

        @Test
        void get_user_by_email_should_return_IllegalArgumentException_when_email_is_null() {

            assertThrows(
                    IllegalArgumentException.class,
                    () -> mockMvc.perform(
                            get(
                                    TEST_USER_EMAIL,
                                    (Object) null
                            )
                    )
            );
        }

        @Test
        void get_user_by_email_should_return_400_when_email_is_wrong()
                throws Exception {

            regTestUser(
                    TEST_USER_EMAIL + 1
            );

            UserLookupDto dto =
                    new UserLookupDto("test");

            String dtoJson =
                    mapper.writeValueAsString(dto);

            UUID requestId =
                    requestIdService.getRequestId();

            MvcResult result =
                    mockMvc.perform(
                                    post(INTERNAL_LOOKUP_URL)
                                            .contentType(
                                                    MediaType.APPLICATION_JSON
                                            )
                                            .header(
                                                    headerRequestIdProps.name(),
                                                    requestId
                                            )
                                            .content(dtoJson)
                            )
                            .andExpect(
                                    status().isBadRequest()
                            )
                            .andReturn();

            checkErrorResponseResultWithoutCheckingValidationErrors(
                    result,
                    HttpStatus.BAD_REQUEST,
                    INTERNAL_LOOKUP_URL
            );
        }

        @Test
        void get_user_by_email_should_return_404_user_not_found()
                throws Exception {

            regTestUser(
                    TEST_USER_EMAIL + 2
            );

            String email =
                    "exampleemail@email.com";

            UserLookupDto dto =
                    new UserLookupDto(email);

            String dtoJson =
                    mapper.writeValueAsString(dto);

            UUID requestId =
                    requestIdService.getRequestId();

            MvcResult result =
                    mockMvc.perform(
                                    post(INTERNAL_LOOKUP_URL)
                                            .contentType(
                                                    MediaType.APPLICATION_JSON
                                            )
                                            .header(
                                                    headerRequestIdProps.name(),
                                                    requestId
                                            )
                                            .content(dtoJson)
                            )
                            .andExpect(
                                    status().isNotFound()
                            )
                            .andReturn();

            checkErrorResponseResultWithoutCheckingValidationErrors(
                    result,
                    HttpStatus.NOT_FOUND,
                    INTERNAL_LOOKUP_URL
            );
        }
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(
                HttpSecurity http
        ) throws Exception {

            return http
                    .csrf(
                            AbstractHttpConfigurer::disable
                    )
                    .authorizeHttpRequests(auth ->
                            auth.anyRequest().permitAll()
                    )
                    .build();
        }
    }
}