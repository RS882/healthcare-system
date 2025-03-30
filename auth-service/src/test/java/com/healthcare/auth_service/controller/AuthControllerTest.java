package com.healthcare.auth_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.healthcare.auth_service.domain.dto.UserInfoDto;
import com.healthcare.auth_service.service.CookieService;
import com.healthcare.auth_service.service.feignClient.UserClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private CookieService cookieService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserClient userClient;

    private ObjectMapper mapper = new ObjectMapper();

    private final String EMAIL = "test@example.com";
    private final String PASSWORD = "136Jkn!kPu5%";
    private final String USER_NAME = "Test User";
    private final Long USER_ID = 1L;
    private final String USER_ROLE = "ROLE_TEST";

    private final String REG_URL = "/api/v1/auth/registration";

    private UserInfoDto userInfoDto;

    @Nested
    @DisplayName("POST " + REG_URL)
    class RegisterUserTests {



        @BeforeEach
        void setUp() {


            userInfoDto = UserInfoDto.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .id(USER_ID)
                    .enabled(true)
                    .roles(Set.of(USER_ROLE))
                    .build();
        }

        @Test
        public void register_user_should_return_201() throws Exception {
//
//            when(userClient.registerUser(any(RegistrationDto.class)))
//                    .thenReturn(userInfoDto);
//
//            String dtoJson = mapper.writeValueAsString(regDto);
//
//            MvcResult result = mockMvc.perform(post(REG_URL)
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(dtoJson))
//                    .andExpect(status().isCreated())
//                    .andReturn();
//            String responseToken = result.getResponse().getContentAsString();
//            JsonNode jsonNodeToken = mapper.readTree(responseToken);
        }
    }
}