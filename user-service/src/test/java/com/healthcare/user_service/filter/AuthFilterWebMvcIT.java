package com.healthcare.user_service.filter;

import com.healthcare.user_service.constant.Role;
import com.healthcare.user_service.filter.security.SignedUserContext;
import com.healthcare.user_service.model.dto.auth.UserAuthInfoDto;
import com.healthcare.user_service.service.interfacies.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_REQUEST_ID;
import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_USER_CONTEXT;
import static com.healthcare.user_service.support.TestConstants.*;
import static com.healthcare.user_service.support.TestDataFactory.randomUserId;
import static com.healthcare.user_service.support.TestDataFactory.requestId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc
@Import({
        AuthFilter.class,
        AuthFilterWebMvcIT.TestConfig.class,
        AuthFilterWebMvcIT.TestSecurityConfig.class
})
@TestPropertySource(properties = {
        "auth-filter.enabled=true",
        "spring.cloud.config.enabled=false"
})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Auth filter integration tests: ")
class AuthFilterWebMvcIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RequestIdFilter requestIdFilter;

    @MockitoBean
    private UserContextFilter userContextFilter;

    private static final String TEST_SECURE_URI = TEST_BASE_URI + SECURE_URI;
    private static final String TEST_PUBLIC_URI = TEST_BASE_URI + PUBLIC_URI;

    private final String VERSION = "1.0";

    @BeforeEach
    void setUpFilters() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(requestIdFilter).doFilter(any(), any(), any());

        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(userContextFilter).doFilter(any(), any(), any());
    }

    @Test
    void should_return_200_when_context_is_valid() throws Exception {
        Long userId = randomUserId();
        String rid = requestId().toString();
        UserAuthInfoDto authInfoDto = new UserAuthInfoDto(
                userId,
                Set.of(Role.ROLE_PATIENT, Role.ROLE_ADMIN)
        );

        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authInfoDto);

        SignedUserContext ctx = new SignedUserContext(
                userId.toString(),
                List.of(Role.ROLE_PATIENT.name(), Role.ROLE_ADMIN.name()),
                rid,
                VERSION
        );

        mockMvc.perform(get(TEST_SECURE_URI)
                        .requestAttr(ATTR_REQUEST_ID, rid)
                        .requestAttr(ATTR_USER_CONTEXT, ctx)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(userId.toString()));
    }

    @Test
    void should_return_401_when_roles_do_not_match() throws Exception {
        Long userId = randomUserId();
        String rid = requestId().toString();
        UserAuthInfoDto authInfoDto = new UserAuthInfoDto(
                userId,
                Set.of( Role.ROLE_ADMIN)
        );

        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authInfoDto);

        SignedUserContext ctx = new SignedUserContext(
                userId.toString(),
                List.of(Role.ROLE_PATIENT.name()),
                rid,
                VERSION
        );

        mockMvc.perform(get(TEST_SECURE_URI)
                        .requestAttr(ATTR_REQUEST_ID, rid)
                        .requestAttr(ATTR_USER_CONTEXT, ctx)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return_401_when_request_id_does_not_match() throws Exception {
        Long userId = randomUserId();
        String rid = requestId().toString();
        String rid_other = requestId().toString();
        UserAuthInfoDto authInfoDto = new UserAuthInfoDto(
                userId,
                Set.of( Role.ROLE_PATIENT)
        );

        when(userService.getUserAuthInfoDtoById(userId)).thenReturn(authInfoDto);

        SignedUserContext ctx = new SignedUserContext(
                userId.toString(),
                List.of(Role.ROLE_PATIENT.name()),
                rid_other,
                VERSION
        );

        mockMvc.perform(get(TEST_SECURE_URI)
                        .requestAttr(ATTR_REQUEST_ID, rid)
                        .requestAttr(ATTR_USER_CONTEXT, ctx)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return_401_when_attributes_are_missing() throws Exception {
        mockMvc.perform(get(TEST_SECURE_URI)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isUnauthorized());
    }

    @RestController
    static class TestController {

        @GetMapping(value = TEST_SECURE_URI, produces = MediaType.TEXT_PLAIN_VALUE)
        public String secure(Authentication authentication) {
            UserAuthInfoDto principal = (UserAuthInfoDto) authentication.getPrincipal();
            return String.valueOf(principal.userId());
        }

        @GetMapping(value = TEST_PUBLIC_URI, produces = MediaType.TEXT_PLAIN_VALUE)
        public String pub() {
            return "public";
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, AuthFilter authFilter) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .httpBasic(Customizer.withDefaults())
                    .formLogin(AbstractHttpConfigurer::disable)
                    .logout(AbstractHttpConfigurer::disable)
                    .exceptionHandling(ex -> ex.authenticationEntryPoint(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                    ))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(TEST_PUBLIC_URI).permitAll()
                            .requestMatchers(TEST_SECURE_URI).authenticated()
                            .anyRequest().permitAll()
                    )
                    .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        }
    }
}