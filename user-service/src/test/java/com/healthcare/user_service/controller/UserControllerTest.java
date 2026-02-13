package com.healthcare.user_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.exception_handler.dto.ErrorResponse;
import com.healthcare.user_service.model.dto.RegistrationDto;
import com.healthcare.user_service.model.dto.UserAuthDto;
import com.healthcare.user_service.model.dto.UserDto;
import com.healthcare.user_service.model.dto.UserLookupDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.stream.Stream;

import static com.healthcare.user_service.controller.API.ApiPaths.LOOKUP_URL;
import static com.healthcare.user_service.controller.API.ApiPaths.REGISTRATION_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("Users controller integration tests: ")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class UserControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MockMvc mockMvc;

    private static final String TEST_USER_NAME = "Test user";
    private static final String TEST_USER_EMAIL = "testexample@gmail.com";
    private static final String TEST_USER_PASSWORD = "136Jkn!kPu5%";

    private ErrorResponse checkErrorResponseResultWithoutCheckingValidationErrors(MvcResult result, HttpStatus status, String url) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        assertNotNull(error.getMessage());
        assertEquals(error.getStatus(), status.value());
        assertEquals(error.getError(), status.getReasonPhrase());
        assertEquals(error.getPath(), url);

        return error;
    }

    private void checkErrorResponseResult(MvcResult result, HttpStatus status, String url) throws Exception {
        ErrorResponse error = checkErrorResponseResultWithoutCheckingValidationErrors(result, status, url);
        assertNull(error.getValidationErrors());
    }

    private void checkErrorResponseResultWithValidationErrors(MvcResult result, HttpStatus status, String url) throws Exception {
        ErrorResponse error = checkErrorResponseResultWithoutCheckingValidationErrors(result, status, url);
        assertFalse(error.getValidationErrors().isEmpty());
    }

    private MvcResult regTestUser() throws Exception {
        return regTestUser(TEST_USER_EMAIL);
    }

    private MvcResult regTestUser(String email) throws Exception {
        RegistrationDto dto = new RegistrationDto(
                email, TEST_USER_NAME, TEST_USER_PASSWORD);

        String dtoJson = mapper.writeValueAsString(dto);

        return mockMvc.perform(post(REGISTRATION_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Nested
    @DisplayName("POST " + REGISTRATION_URL)
    class RegUserTest {

        private void checkErrorResponseResultWithValidationErrors(MvcResult result, HttpStatus status) throws Exception {
            UserControllerTest.this.checkErrorResponseResultWithValidationErrors(result, status, REGISTRATION_URL);
        }

        @Test
        public void registration_user_should_return_201() throws Exception {

            String email = TEST_USER_EMAIL + 0;
            MvcResult result = regTestUser(email);

            String jsonResponse = result.getResponse().getContentAsString();
            UserDto responseDto = mapper.readValue(jsonResponse, UserDto.class);

            Long id = responseDto.getId();
            assertThat(id).isNotNull();
            assertThat(id).isInstanceOf(Long.class);
            assertEquals(email, responseDto.getEmail());
            assertEquals(TEST_USER_NAME, responseDto.getName());
            Optional<String> firstRole = responseDto.getRoles().stream().findFirst();
            assertTrue(firstRole.isPresent());
            assertEquals(Role.ROLE_PATIENT.name().toLowerCase(), firstRole.get().toLowerCase());
            assertTrue(responseDto.isEnabled());
        }


        @ParameterizedTest(name = "Тест {index}:registration_with_status_400_registration_data_is_incorrect [{arguments}]")
        @MethodSource("incorrectLoginData")
        public void registration_user_should_return_400_when_registration_data_is_wrong(RegistrationDto dto) throws Exception {

            String dtoJson = mapper.writeValueAsString(dto);

            MvcResult result = mockMvc.perform(post(REGISTRATION_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            checkErrorResponseResultWithValidationErrors(result, HttpStatus.BAD_REQUEST);
        }

        private static Stream<Arguments> incorrectLoginData() {
            return Stream.of(Arguments.of(
                            new RegistrationDto(
                                    null,
                                    TEST_USER_NAME,
                                    TEST_USER_PASSWORD)),
                    Arguments.of(
                            new RegistrationDto(
                                    "TEST_USER_EMAIL",
                                    TEST_USER_NAME,
                                    TEST_USER_PASSWORD)),
                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    null,
                                    TEST_USER_PASSWORD)),
                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    "   ",
                                    TEST_USER_PASSWORD)),
                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    "tt",
                                    TEST_USER_PASSWORD)),
                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    "skdfsdfjsldfjsldfaksjlaskahfkahsflkahsfkashflkahskfaskhfalksfaksflashfaskhlhfklashfsaklfhlkafshlsafhfaslhfka",
                                    TEST_USER_PASSWORD)),
                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    TEST_USER_NAME,
                                    null)),
                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    TEST_USER_NAME,
                                    "       ")),
                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    TEST_USER_NAME,
                                    "1E")),
                    Arguments.of(
                            new RegistrationDto(
                                    TEST_USER_EMAIL,
                                    TEST_USER_NAME,
                                    "skdfsdfjsldfjsldfaksjlaskahfkahsflkahsfkashflkahskfaskhfalksfaksflashfaskhlhfklashfsaklfhlkafshlsafhfaslhfka")),
                    Arguments.of(
                            new RegistrationDto(
                                    "TEST_USER_EMAIL",
                                    "   ",
                                    "1E"))
            );
        }

    }

    @Nested
    @DisplayName("POST " + LOOKUP_URL)
    class UserByEmailTest {

        @Test
        public void get_user_auth_should_return_200() throws Exception {
            regTestUser();

            UserLookupDto dto = new UserLookupDto(TEST_USER_EMAIL);

            String dtoJson = mapper.writeValueAsString(dto);

            MvcResult result = mockMvc.perform(post(LOOKUP_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isOk())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            UserAuthDto responseDto = mapper.readValue(jsonResponse, UserAuthDto.class);

            Long id = responseDto.getId();
            assertThat(id).isNotNull();
            assertThat(id).isInstanceOf(Long.class);

            String password = responseDto.getPassword();
            assertThat(password).isNotNull();
            assertThat(password).isInstanceOf(String.class);

            assertEquals(TEST_USER_EMAIL, responseDto.getEmail());

            Optional<String> firstRole = responseDto.getRoles().stream().findFirst();
            assertTrue(firstRole.isPresent());
            assertEquals(Role.ROLE_PATIENT.name(), firstRole.get());
            assertTrue(responseDto.isEnabled());
        }

        @Test
        public void get_user_by_email_should_return_IllegalArgumentException_when_email_is_null() throws Exception {
            assertThrows(IllegalArgumentException.class, () -> {
                mockMvc.perform(get(TEST_USER_EMAIL, (Object) null));
            });
        }

        @Test
        public void get_user_by_email_should_return_400_when_email_is_wrong() throws Exception {
            regTestUser(TEST_USER_EMAIL + 1);

            UserLookupDto dto = new UserLookupDto("test");

            String dtoJson = mapper.writeValueAsString(dto);

            MvcResult result = mockMvc.perform(post(LOOKUP_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isBadRequest())
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
            HttpStatus status = HttpStatus.BAD_REQUEST;
            assertNotNull(error.getMessage());
            assertEquals(error.getStatus(), status.value());
            assertEquals(error.getError(), status.getReasonPhrase());
            assertEquals(error.getPath(), LOOKUP_URL);
        }

        @Test
        public void get_user_by_email_should_return_404_user_not_found() throws Exception {
            regTestUser(TEST_USER_EMAIL + 2);
            String email = "exampleemail@email.com";

            UserLookupDto dto = new UserLookupDto(email);

            String dtoJson = mapper.writeValueAsString(dto);
            MvcResult result = mockMvc.perform(post(LOOKUP_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isNotFound())
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
            HttpStatus status = HttpStatus.NOT_FOUND;
            assertNotNull(error.getMessage());
            assertEquals(error.getStatus(), status.value());
            assertEquals(error.getError(), status.getReasonPhrase());
            assertEquals(error.getPath(), LOOKUP_URL);
        }
    }
}