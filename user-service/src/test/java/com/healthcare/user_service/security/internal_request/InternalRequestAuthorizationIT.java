package com.healthcare.user_service.security.internal_request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.config.AbstractKafkaRedisMsqlTestContainer;
import com.healthcare.user_service.model.dto.auth.UserAuthDto;
import com.healthcare.user_service.model.dto.request.UserLookupDto;
import com.healthcare.user_service.security.internal_request.constant.InternalService;
import com.healthcare.user_service.security.internal_request.dto.InternalRequestGrant;
import com.healthcare.user_service.security.internal_request.properties.InternalRequestConsumerProperties;
import com.healthcare.user_service.service.interfacies.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static com.healthcare.user_service.controller.API.ApiPaths.INTERNAL_LOOKUP_URL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@TestPropertySource(properties = {
        "internal-request-filter.enabled=true",

        "user-context-filter.enabled=false",
        "auth-filter.enabled=false",
        "request-id-filter.enabled=false",

        "spring.kafka.listener.auto-startup=false",
        "app.outbox.publisher.enabled=false",
        "spring.cloud.config.enabled=false",

        "internal-request.allowed-issuers[0]=auth-service",
        "internal-request.allowed-issuers[1]=ai-service"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Internal request authorization integration tests")
class InternalRequestAuthorizationIT
        extends AbstractKafkaRedisMsqlTestContainer {

    private static final Duration TEST_GRANT_TTL =
            Duration.ofSeconds(30);

    private static final Long USER_ID = 100L;

    private static final String EMAIL =
            "internal-test@example.com";

    private static final String ROLE = "ROLE_PATIENT";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternalRequestConsumerProperties internalRequestProperties;

    @MockitoBean
    private UserService userService;

    @Test
    void should_allow_auth_service_to_call_internal_lookup_with_valid_grant()
            throws Exception {

        UserAuthDto response = UserAuthDto.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password("encoded-password")
                .roles(Set.of(ROLE))
                .enabled(true)
                .build();

        when(userService.getUserInfoByEmail(EMAIL))
                .thenReturn(response);

        UUID internalRequestId = UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(InternalService.AUTH_SERVICE)
                        .target(internalRequestProperties.serviceName())
                        .method(HttpMethod.POST.name())
                        .path(INTERNAL_LOOKUP_URL)
                        .createdAt(Instant.now())
                        .build();

        saveGrant(internalRequestId, grant);

        performInternalLookup(internalRequestId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.roles[0]").value(ROLE));

        verify(userService).getUserInfoByEmail(EMAIL);
    }

    @Test
    void should_reject_reused_internal_request_id()
            throws Exception {

        UserAuthDto response = UserAuthDto.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password("encoded-password")
                .roles(Set.of())
                .enabled(true)
                .build();

        when(userService.getUserInfoByEmail(EMAIL))
                .thenReturn(response);

        UUID internalRequestId = UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(InternalService.AUTH_SERVICE)
                        .target(internalRequestProperties.serviceName())
                        .method(HttpMethod.POST.name())
                        .path(INTERNAL_LOOKUP_URL)
                        .createdAt(Instant.now())
                        .build();

        saveGrant(internalRequestId, grant);

        String redisKey = internalRequestProperties.keyPrefix() + internalRequestId;


        performInternalLookup(internalRequestId)
                .andExpect(status().isOk());

        Boolean grantStillExists = redisTemplate.hasKey(redisKey);

        assertThat(grantStillExists).isFalse();

        performInternalLookup(internalRequestId)
                .andExpect(status().isUnauthorized());

        verify(userService, times(1)).getUserInfoByEmail(EMAIL);
    }

    @Test
    void should_return_401_when_internal_request_id_header_is_missing()
            throws Exception {

        UserLookupDto request =
                new UserLookupDto(EMAIL);

        mockMvc.perform(
                        post(INTERNAL_LOOKUP_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void should_return_401_when_internal_request_id_is_not_uuid()
            throws Exception {

        UserLookupDto request =
                new UserLookupDto(EMAIL);

        mockMvc.perform(
                        post(INTERNAL_LOOKUP_URL)
                                .header(
                                        internalRequestProperties.headerName(),
                                        "not-a-valid-uuid"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void should_return_401_when_internal_request_grant_is_not_found()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        String redisKey =
                internalRequestProperties.keyPrefix()
                        + internalRequestId;

        assertThat(redisTemplate.hasKey(redisKey))
                .isFalse();

        performInternalLookup(internalRequestId)
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void should_return_401_when_internal_request_target_is_wrong()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(InternalService.AUTH_SERVICE)
                        .target("wrong-service")
                        .method(HttpMethod.POST.name())
                        .path(INTERNAL_LOOKUP_URL)
                        .createdAt(Instant.now())
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        performInternalLookup(internalRequestId)
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void should_return_401_when_internal_request_method_is_wrong()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(InternalService.AUTH_SERVICE)
                        .target(internalRequestProperties.serviceName())
                        .method(HttpMethod.GET.name())
                        .path(INTERNAL_LOOKUP_URL)
                        .createdAt(Instant.now())
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        performInternalLookup(internalRequestId)
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void should_return_401_when_internal_request_path_is_wrong()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(InternalService.AUTH_SERVICE)
                        .target(internalRequestProperties.serviceName())
                        .method(HttpMethod.POST.name())
                        .path("/v1/users/internal/wrong")
                        .createdAt(Instant.now())
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        performInternalLookup(internalRequestId)
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void should_return_403_when_issuer_is_allowed_but_has_no_user_lookup_authority()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(InternalService.AI_SERVICE)
                        .target(internalRequestProperties.serviceName())
                        .method(HttpMethod.POST.name())
                        .path(INTERNAL_LOOKUP_URL)
                        .createdAt(Instant.now())
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        performInternalLookup(internalRequestId)
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void should_allow_ai_service_to_access_user_auth_info()
            throws Exception {

        long userId = 1L;

        String path =
                "/v1/users/internal/"
                        + userId
                        + "/auth-info";

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AI_SERVICE
                        )
                        .target(
                                "user-service"
                        )
                        .method(
                                HttpMethod.GET.name()
                        )
                        .path(
                                path
                        )
                        .createdAt(
                                Instant.now()
                        )
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        mockMvc.perform(
                        get(path)
                                .header(
                                        "X-Test-Internal-Request-Id",
                                        internalRequestId
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void should_forbid_auth_service_from_accessing_user_auth_info()
            throws Exception {

        long userId = 1L;

        String path =
                "/v1/users/internal/"
                        + userId
                        + "/auth-info";

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AUTH_SERVICE
                        )
                        .target(
                                "user-service"
                        )
                        .method(
                                HttpMethod.GET.name()
                        )
                        .path(
                                path
                        )
                        .createdAt(
                                Instant.now()
                        )
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        mockMvc.perform(
                        get(path)
                                .header(
                                        "X-Test-Internal-Request-Id",
                                        internalRequestId
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void should_forbid_ai_service_from_accessing_internal_user_lookup()
            throws Exception {

        String path =
                "/v1/users/internal/lookup";

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AI_SERVICE
                        )
                        .target(
                                "user-service"
                        )
                        .method(
                                HttpMethod.POST.name()
                        )
                        .path(
                                path
                        )
                        .createdAt(
                                Instant.now()
                        )
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        mockMvc.perform(
                        post(path)
                                .header(
                                        "X-Test-Internal-Request-Id",
                                        internalRequestId
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                    {
                                      "userEmail": "test@example.com"
                                    }
                                    """)
                )
                .andExpect(
                        status().isForbidden()
                );
    }

    @Test
    void should_reject_replayed_ai_service_internal_request()
            throws Exception {

        long userId = 1L;

        String path =
                "/v1/users/internal/"
                        + userId
                        + "/auth-info";

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AI_SERVICE
                        )
                        .target(
                                "user-service"
                        )
                        .method(
                                HttpMethod.GET.name()
                        )
                        .path(
                                path
                        )
                        .createdAt(
                                Instant.now()
                        )
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        mockMvc.perform(
                        get(path)
                                .header(
                                        "X-Test-Internal-Request-Id",
                                        internalRequestId
                                )
                )
                .andExpect(
                        status().isOk()
                );

        mockMvc.perform(
                        get(path)
                                .header(
                                        "X-Test-Internal-Request-Id",
                                        internalRequestId
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void should_reject_ai_service_grant_for_different_user_path()
            throws Exception {

        long grantedUserId = 10L;
        long requestedUserId = 11L;

        String grantedPath =
                "/v1/users/internal/"
                        + grantedUserId
                        + "/auth-info";

        String requestedPath =
                "/v1/users/internal/"
                        + requestedUserId
                        + "/auth-info";

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AI_SERVICE
                        )
                        .target(
                                "user-service"
                        )
                        .method(
                                HttpMethod.GET.name()
                        )
                        .path(
                                grantedPath
                        )
                        .createdAt(
                                Instant.now()
                        )
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        mockMvc.perform(
                        get(requestedPath)
                                .header(
                                        "X-Test-Internal-Request-Id",
                                        internalRequestId
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    @Test
    void should_reject_ai_service_grant_with_wrong_http_method()
            throws Exception {

        long userId = 1L;

        String path =
                "/v1/users/internal/"
                        + userId
                        + "/auth-info";

        UUID internalRequestId =
                UUID.randomUUID();

        InternalRequestGrant grant =
                InternalRequestGrant.builder()
                        .issuer(
                                InternalService.AI_SERVICE
                        )
                        .target(
                                "user-service"
                        )
                        .method(
                                HttpMethod.GET.name()
                        )
                        .path(
                                path
                        )
                        .createdAt(
                                Instant.now()
                        )
                        .build();

        saveGrant(
                internalRequestId,
                grant
        );

        mockMvc.perform(
                        post(path)
                                .header(
                                        "X-Test-Internal-Request-Id",
                                        internalRequestId
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }

    private ResultActions performInternalLookup(
            UUID internalRequestId
    ) throws Exception {

        UserLookupDto request = new UserLookupDto(EMAIL);

        return mockMvc.perform(
                post(INTERNAL_LOOKUP_URL)
                        .header(
                                internalRequestProperties.headerName(),
                                internalRequestId.toString()
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );
    }

    private void saveGrant(
            UUID internalRequestId,
            InternalRequestGrant grant
    ) throws Exception {

        String redisKey =
                internalRequestProperties.keyPrefix()
                        + internalRequestId;

        String serializedGrant =
                objectMapper.writeValueAsString(grant);

        redisTemplate
                .opsForValue()
                .set(
                        redisKey,
                        serializedGrant,
                        TEST_GRANT_TTL
                );
    }
}