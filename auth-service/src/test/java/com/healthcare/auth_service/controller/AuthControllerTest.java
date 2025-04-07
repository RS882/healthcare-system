package com.healthcare.auth_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.auth_service.domain.dto.LoginDto;
import com.healthcare.auth_service.domain.dto.TokensDto;
import com.healthcare.auth_service.domain.dto.UserInfoDto;
import com.healthcare.auth_service.exception_handler.dto.ErrorResponse;
import com.healthcare.auth_service.service.JwtService;
import com.healthcare.auth_service.service.feignClient.UserClient;
import com.healthcare.auth_service.service.interfacies.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.healthcare.auth_service.service.CookieService.REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("Auth controller integration tests: ")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayNameGeneration(value = DisplayNameGenerator.ReplaceUnderscores.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @Value("${prefix.refresh}")
    private String refreshPrefix;

    @Value("${jwt.max-tokens}")
    private int maxTokens;

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "136Jkn!kPu5%";
    private static final Long USER_ID = 1L;
    private static final String USER_ROLE = "ROLE_TEST";

    private final String LOGIN_URL = "/api/v1/auth/login";
    private final String REFRESH_URL = "/api/v1/auth/refresh";
    private final String LOGOUT_URL = "/api/v1/auth/logout";

    @AfterEach
    void afterEach() {
        Set<String> keys = redis.keys("test" + "*");
        redis.delete(keys);
    }

    private Cookie getCookie() throws Exception {

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .email(EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .id(USER_ID)
                .enabled(true)
                .roles(Set.of(USER_ROLE))
                .build();

        UserDetails userDetail = new User(
                EMAIL,
                userInfoDto.getPassword(),
                userInfoDto.isEnabled(),
                true,
                true,
                true,
                userInfoDto.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList());

        LoginDto loginDto = LoginDto.builder()
                .password(PASSWORD)
                .userEmail(EMAIL)
                .build();

        when(userClient.getUserByEmail(any(String.class)))
                .thenReturn(userInfoDto);

        String dtoJson = mapper.writeValueAsString(loginDto);

        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie(REFRESH_TOKEN);
    }

    private TokensDto loginUser() throws Exception {
        UserInfoDto userInfoDto = UserInfoDto.builder()
                .email(EMAIL)
                .password(passwordEncoder.encode(PASSWORD))
                .id(USER_ID)
                .enabled(true)
                .roles(Set.of(USER_ROLE))
                .build();

        LoginDto loginDto = LoginDto.builder()
                .password(PASSWORD)
                .userEmail(EMAIL)
                .build();

        when(userClient.getUserByEmail(any(String.class)))
                .thenReturn(userInfoDto);

        String dtoJson = mapper.writeValueAsString(loginDto);

        MvcResult result = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseToken = result.getResponse().getContentAsString();
        JsonNode jsonNodeToken = mapper.readTree(responseToken);
        String accessToken = jsonNodeToken.get("accessToken").asText();
        String refreshToken = Objects.requireNonNull(
                        result.getResponse().getCookie(REFRESH_TOKEN))
                .getValue();
        return TokensDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

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

    private String getKey() {
        return refreshPrefix + USER_ID;
    }

    @Nested
    @DisplayName("POST " + LOGIN_URL)
    class LoginUserTests {

        private void checkErrorResponseResult(MvcResult result, HttpStatus status) throws Exception {
            AuthControllerTest.this.checkErrorResponseResult(result, status, LOGIN_URL);

            assertFalse(redis.hasKey(getKey()));
        }

        private void checkErrorResponseResultWithValidationErrors(MvcResult result, HttpStatus status) throws Exception {
            AuthControllerTest.this.checkErrorResponseResultWithValidationErrors(result, status, LOGIN_URL);

            assertFalse(redis.hasKey(getKey()));
        }

        @Test
        public void login_user_should_return_200() throws Exception {

            UserDetails userDetail = new User(
                    EMAIL,
                    passwordEncoder.encode(PASSWORD),
                    true,
                    true,
                    true,
                    true,
                    Set.of(USER_ROLE).stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList());

            TokensDto tokensDto = loginUser();
            String accessToken = tokensDto.getAccessToken();
            String refreshToken = tokensDto.getRefreshToken();

            assertTrue(jwtService.validateAccessToken(accessToken, userDetail));
            assertTrue(jwtService.validateRefreshToken(refreshToken, userDetail));

            assertTrue(redis.hasKey(getKey() + ":" + refreshToken));
        }

        @ParameterizedTest(name = "Тест {index}: login_with_status_400_login_data_is_incorrect [{arguments}]")
        @MethodSource("incorrectLoginData")
        public void login_user_should_return_400_when_login_data_is_wrong(LoginDto loginDto) throws Exception {

            String dtoJson = mapper.writeValueAsString(loginDto);

            MvcResult result = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            checkErrorResponseResultWithValidationErrors(result, HttpStatus.BAD_REQUEST);
        }

        private static Stream<Arguments> incorrectLoginData() {
            return Stream.of(Arguments.of(
                            LoginDto.builder()
                                    .userEmail("testexample?com")
                                    .password(PASSWORD)
                                    .build()),
                    Arguments.of(
                            LoginDto.builder()
                                    .password(PASSWORD)
                                    .build()),
                    Arguments.of(
                            LoginDto.builder()
                                    .userEmail(EMAIL)
                                    .build()),
                    Arguments.of(
                            LoginDto.builder()
                                    .userEmail(EMAIL)
                                    .password("1E")
                                    .build()),
                    Arguments.of(
                            LoginDto.builder()
                                    .userEmail(EMAIL)
                                    .password("asdasdlDFsd90q!u023402lks@djalsdajsd#lahsdkahs$$%dllkasd")
                                    .build()),
                    Arguments.of(
                            LoginDto.builder()
                                    .userEmail(EMAIL)
                                    .password("asdasdlweqwe")
                                    .build()),
                    Arguments.of(
                            LoginDto.builder()
                                    .userEmail(EMAIL)
                                    .password("asda@sdlweqwe")
                                    .build()),
                    Arguments.of(
                            LoginDto.builder()
                                    .userEmail(EMAIL)
                                    .password("asdasdlwe8qwe")
                                    .build()),
                    Arguments.of(
                            LoginDto.builder()
                                    .userEmail(EMAIL)
                                    .password("Qsdasdlwe8qwe")
                                    .build()),
                    Arguments.of(
                            LoginDto.builder()
                                    .userEmail("testexample?com")
                                    .password("Qsdasdlwe8qwe")
                                    .build())
            );
        }

        @Test
        public void login_user_should_return_404_when_user_service_get_exception() throws Exception {

            when(userClient.getUserByEmail(any(String.class)))
                    .thenThrow(new RuntimeException("Something went wrong"));

            LoginDto loginDto = LoginDto.builder()
                    .password(PASSWORD)
                    .userEmail(EMAIL)
                    .build();

            String dtoJson = mapper.writeValueAsString(loginDto);

            MvcResult result = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isNotFound())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.NOT_FOUND);
        }

        @Test
        public void login_user_should_return_404_when_user_service_get_null() throws Exception {

            when(userClient.getUserByEmail(any(String.class)))
                    .thenReturn(null);

            LoginDto loginDto = LoginDto.builder()
                    .password(PASSWORD)
                    .userEmail(EMAIL)
                    .build();

            String dtoJson = mapper.writeValueAsString(loginDto);

            MvcResult result = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isNotFound())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.NOT_FOUND);
        }

        @Test
        public void login_user_should_return_403_when_user_is_disable() throws Exception {

            UserInfoDto userInfoDto = UserInfoDto.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .id(USER_ID)
                    .enabled(false)
                    .roles(Set.of(USER_ROLE))
                    .build();

            when(userClient.getUserByEmail(any(String.class)))
                    .thenReturn(userInfoDto);

            LoginDto loginDto = LoginDto.builder()
                    .password(PASSWORD)
                    .userEmail(EMAIL)
                    .build();

            String dtoJson = mapper.writeValueAsString(loginDto);

            MvcResult result = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isForbidden())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.FORBIDDEN);
        }

        @ParameterizedTest(name = "Тест {index}: login_with_status_400_when_user_service_get_incorrect_user_info_dto[{arguments}]")
        @MethodSource("incorrectUserInfo")
        public void login_user_should_return_400_when_user_service_get_incorrect_user_info_dto(UserInfoDto userInfoDto) throws Exception {

            when(userClient.getUserByEmail(any(String.class)))
                    .thenReturn(userInfoDto);

            LoginDto loginDto = LoginDto.builder()
                    .password(PASSWORD)
                    .userEmail(EMAIL)
                    .build();

            String dtoJson = mapper.writeValueAsString(loginDto);

            MvcResult result = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            checkErrorResponseResultWithValidationErrors(result, HttpStatus.BAD_REQUEST);
        }

        private static Stream<Arguments> incorrectUserInfo() {

            return Stream.of(Arguments.of(
                            UserInfoDto.builder()
                                    .email(EMAIL)
                                    .password(PASSWORD)
                                    .enabled(true)
                                    .roles(Set.of(USER_ROLE))
                                    .build()),
                    Arguments.of(UserInfoDto.builder()
                            .password(PASSWORD)
                            .id(USER_ID)
                            .enabled(true)
                            .roles(Set.of(USER_ROLE))
                            .build()),
                    Arguments.of(UserInfoDto.builder()
                            .email("testexample?com")
                            .password(PASSWORD)
                            .id(USER_ID)
                            .enabled(true)
                            .roles(Set.of(USER_ROLE))
                            .build()),
                    Arguments.of(UserInfoDto.builder()
                            .email(EMAIL)
                            .id(USER_ID)
                            .enabled(true)
                            .roles(Set.of(USER_ROLE))
                            .build()),
                    Arguments.of(UserInfoDto.builder()
                            .email(EMAIL)
                            .password("")
                            .id(USER_ID)
                            .enabled(true)
                            .roles(Set.of(USER_ROLE))
                            .build()),
                    Arguments.of(UserInfoDto.builder()
                            .email(EMAIL)
                            .password("         ")
                            .id(USER_ID)
                            .enabled(true)
                            .roles(Set.of(USER_ROLE))
                            .build()),
                    Arguments.of(UserInfoDto.builder()
                            .email(EMAIL)
                            .password(PASSWORD)
                            .id(USER_ID)
                            .enabled(true)
                            .roles(new HashSet<>())
                            .build()),
                    Arguments.of(UserInfoDto.builder()
                            .email(EMAIL)
                            .password(PASSWORD)
                            .id(USER_ID)
                            .enabled(true)
                            .build()),
                    Arguments.of(UserInfoDto.builder()
                            .enabled(true)
                            .build()),
                    Arguments.of(UserInfoDto.builder()
                            .email("EMAIL")
                            .password("")
                            .enabled(true)
                            .roles(new HashSet<>())
                            .build())
            );
        }


        @Test
        public void login_user_should_return_403_when_user_is_blocked() throws Exception {

            UserInfoDto userInfoDto = UserInfoDto.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .id(USER_ID)
                    .enabled(true)
                    .roles(Set.of(USER_ROLE))
                    .build();

            when(userClient.getUserByEmail(any(String.class)))
                    .thenReturn(userInfoDto);

            LoginDto loginDto = LoginDto.builder()
                    .password(PASSWORD)
                    .userEmail(EMAIL)
                    .build();

            String dtoJson = mapper.writeValueAsString(loginDto);

            for (int i = 0; i < maxTokens; i++) {
                Thread.sleep(1000);
                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(dtoJson))
                        .andExpect(status().isOk());
            }

            MvcResult result = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isForbidden())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.FORBIDDEN);
        }


        @Test
        public void login_user_should_return_401_when_password_is_wrong() throws Exception {

            UserInfoDto userInfoDto = UserInfoDto.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .id(USER_ID)
                    .enabled(true)
                    .roles(Set.of(USER_ROLE))
                    .build();

            when(userClient.getUserByEmail(any(String.class)))
                    .thenReturn(userInfoDto);

            LoginDto loginDto = LoginDto.builder()
                    .password(PASSWORD + "wrong")
                    .userEmail(EMAIL)
                    .build();

            String dtoJson = mapper.writeValueAsString(loginDto);

            MvcResult result = mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dtoJson))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("POST " + REFRESH_URL)
    class RefreshTests {

        private void checkErrorResponseResult(MvcResult result, HttpStatus status) throws Exception {
            AuthControllerTest.this.checkErrorResponseResult(result, status, REFRESH_URL);

            assertFalse(redis.hasKey(getKey()));
        }

        @Test
        public void refresh_should_return_200() throws Exception {

            UserInfoDto userInfoDto = UserInfoDto.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .id(USER_ID)
                    .enabled(true)
                    .roles(Set.of(USER_ROLE))
                    .build();

            UserDetails userDetail = new User(
                    EMAIL,
                    userInfoDto.getPassword(),
                    userInfoDto.isEnabled(),
                    true,
                    true,
                    true,
                    userInfoDto.getRoles().stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList());

            Cookie cookie = getCookie();

            when(userClient.getUserByEmail(any(String.class)))
                    .thenReturn(userInfoDto);

            Thread.sleep(1000);
            MvcResult result = mockMvc.perform(post(REFRESH_URL)
                            .cookie(cookie))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseToken = result.getResponse().getContentAsString();
            JsonNode jsonNodeToken = mapper.readTree(responseToken);
            String accessToken = jsonNodeToken.get("accessToken").asText();
            String refreshToken = Objects.requireNonNull(
                            result.getResponse().getCookie(REFRESH_TOKEN))
                    .getValue();

            assertTrue(jwtService.validateAccessToken(accessToken, userDetail));
            assertTrue(jwtService.validateRefreshToken(refreshToken, userDetail));

            assertTrue(redis.hasKey(getKey() + ":" + refreshToken));

            assertFalse(redis.hasKey(getKey() + ":" + cookie.getValue()));
        }

        @Test
        public void refresh_should_return_400_when_cookie_is_null() throws Exception {
            mockMvc.perform(post(REFRESH_URL))
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void refresh_with_status_401_cookie_is_incorrect() throws Exception {
            Cookie cookie = new Cookie("test", "test");
            MvcResult result = mockMvc.perform(get(REFRESH_URL)
                            .cookie(cookie))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.UNAUTHORIZED);
        }

        @Test
        public void refresh_with_status_401_token_is_incorrect() throws Exception {
            Cookie cookie = new Cookie(REFRESH_TOKEN, "test");
            MvcResult result = mockMvc.perform(get(REFRESH_URL)
                            .cookie(cookie))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.UNAUTHORIZED);
        }

        @Test
        public void refresh_with_status_401_token_is_not_found() throws Exception {

            UserDetails userDetail = new User(
                    EMAIL,
                    passwordEncoder.encode(PASSWORD),
                    true,
                    true,
                    true,
                    true,
                    Set.of(USER_ROLE).stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList());

            TokensDto tokens = jwtService.getTokens(userDetail, USER_ID);

            Cookie cookie = new Cookie(REFRESH_TOKEN, tokens.getRefreshToken());

            MvcResult result = mockMvc.perform(get(REFRESH_URL)
                            .cookie(cookie))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("POST" + LOGOUT_URL)
    class LogoutTests {

        private void checkErrorResponseResult(MvcResult result, HttpStatus status) throws Exception {
            AuthControllerTest.this.checkErrorResponseResult(result, status, LOGOUT_URL);
        }

        @Test
        public void logout_should_return_204() throws Exception {
            TokensDto tokens = loginUser();
            String accessToken = tokens.getAccessToken();
            String refreshToken = tokens.getRefreshToken();

            Cookie cookie = new Cookie(REFRESH_TOKEN, refreshToken);

            mockMvc.perform(post(LOGOUT_URL)
                            .cookie(cookie)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isNoContent());

            assertFalse(redis.hasKey(getKey() + ":" + refreshToken));

            assertTrue(tokenBlacklistService.isBlacklisted(accessToken));

            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        public void logout_should_return_401_header_authorization_is_null() throws Exception {
            TokensDto tokens = loginUser();

            String accessToken = tokens.getAccessToken();
            String refreshToken = tokens.getRefreshToken();

            Cookie cookie = new Cookie(REFRESH_TOKEN, refreshToken);

            MvcResult result = mockMvc.perform(post(LOGOUT_URL)
                            .cookie(cookie))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.UNAUTHORIZED);

            assertTrue(redis.hasKey(getKey() + ":" + refreshToken));
            assertFalse(tokenBlacklistService.isBlacklisted(accessToken));
        }

        @Test
        public void logout_should_return_401_header_authorization_is_not_bearer() throws Exception {
            TokensDto tokens = loginUser();

            String accessToken = tokens.getAccessToken();
            String refreshToken = tokens.getRefreshToken();

            Cookie cookie = new Cookie(REFRESH_TOKEN, refreshToken);

            MvcResult result = mockMvc.perform(post(LOGOUT_URL)
                            .cookie(cookie)
                            .header(HttpHeaders.AUTHORIZATION, "Test " + accessToken))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.UNAUTHORIZED);

            assertTrue(redis.hasKey(getKey() + ":" + refreshToken));
            assertFalse(tokenBlacklistService.isBlacklisted(accessToken));
        }


        @Test
        public void logout_should_return_401_token_is_incorrect() throws Exception {
            TokensDto tokens = loginUser();

            String accessToken = tokens.getAccessToken();
            String refreshToken = tokens.getRefreshToken();

            Cookie cookie = new Cookie(REFRESH_TOKEN, refreshToken);

            MvcResult result = mockMvc.perform(post(LOGOUT_URL)
                            .cookie(cookie)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + "test_wrong_token"))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.UNAUTHORIZED);

            assertTrue(redis.hasKey(getKey() + ":" + refreshToken));
            assertFalse(tokenBlacklistService.isBlacklisted(accessToken));
        }

        @Test
        public void logout_should_return_400_when_cookie_is_null() throws Exception {
            TokensDto tokens = loginUser();

            String accessToken = tokens.getAccessToken();

            mockMvc.perform(post(LOGOUT_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
            ;
        }

        @Test
        public void logout_with_status_400_cookie_is_incorrect() throws Exception {
            TokensDto tokens = loginUser();

            String accessToken = tokens.getAccessToken();

            Cookie cookie = new Cookie("test", "test");
             mockMvc.perform(post(LOGOUT_URL)
                            .cookie(cookie)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isBadRequest());
        }

        @Test
        public void logout_with_status_401_token_is_incorrect() throws Exception {
            TokensDto tokens = loginUser();

            String accessToken = tokens.getAccessToken();

            Cookie cookie = new Cookie(REFRESH_TOKEN, "test");
            MvcResult result = mockMvc.perform(post(LOGOUT_URL)
                            .cookie(cookie)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.UNAUTHORIZED);
        }

        @Test
        public void logout_with_status_401_token_is_not_found() throws Exception {
            TokensDto tokens = loginUser();

            String accessToken = tokens.getAccessToken();
            String refreshToken = tokens.getRefreshToken();

            redis.delete(getKey() + ":" + refreshToken);

            Cookie cookie = new Cookie(REFRESH_TOKEN, refreshToken);

            MvcResult result = mockMvc.perform(post(LOGOUT_URL)
                            .cookie(cookie)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isUnauthorized())
                    .andReturn();

            checkErrorResponseResult(result, HttpStatus.UNAUTHORIZED);
        }
    }
}