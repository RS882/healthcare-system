package com.healthcare.aiservice.security;


import com.healthcare.aiservice.cache.CacheNames;
import com.healthcare.aiservice.config.AbstractMongoRedisIntegrationTest;
import com.healthcare.aiservice.security.constant.Role;
import com.healthcare.aiservice.security.dto.UserAuthInfoDto;
import com.healthcare.aiservice.security.feign_client.UserAuthInfoClient;
import com.healthcare.aiservice.security.filter.security.interfaces.UserContextVerifier;
import com.healthcare.aiservice.security.properties.HeaderRequestIdProperties;
import com.healthcare.aiservice.security.properties.RequestIdProperties;
import com.healthcare.aiservice.security.properties.UserContextProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityAuthenticationIntegrationTest.TestControllerConfiguration.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Security authentication integration tests: ")
class SecurityAuthenticationIntegrationTest
        extends AbstractMongoRedisIntegrationTest {

    private static final String TEST_URL =
            "/test/security/current-user";

    private static final long USER_ID =
            42L;

    private static final String USER_ID_AS_STRING =
            "42";

    private static final String SIGNED_USER_CONTEXT =
            "signed-user-context-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private HeaderRequestIdProperties
            headerRequestIdProperties;

    @Autowired
    private UserContextProperties
            userContextProperties;

    @Autowired
    private RequestIdProperties
            requestIdProperties;

    @MockitoBean
    private UserContextVerifier userContextVerifier;

    @MockitoBean
    private UserAuthInfoClient userAuthInfoClient;

    @BeforeEach
    void setUp() {
        Cache cache =
                cacheManager.getCache(
                        CacheNames.USER_AUTH_INFO
                );

        assertThat(cache)
                .isNotNull();

        cache.clear();

        reset(
                userContextVerifier,
                userAuthInfoClient
        );
    }

    @Test
    void request_ShouldAuthenticateUser_AndPopulateSecurityContext()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        saveRequestIdInRedis(requestId);

        Claims claims = createClaims(
                requestId,
                List.of(
                        Role.ROLE_PATIENT.name(),
                        Role.ROLE_ADMIN.name()
                )
        );

        UserAuthInfoDto authInfo =
                new UserAuthInfoDto(
                        USER_ID,
                        Set.of(
                                Role.ROLE_PATIENT,
                                Role.ROLE_ADMIN
                        )
                );

        when(userContextVerifier.verifyAndGetClaims(
                SIGNED_USER_CONTEXT
        )).thenReturn(claims);

        when(userAuthInfoClient.getUserAuthInfo(USER_ID))
                .thenReturn(authInfo);

        mockMvc.perform(
                        get(TEST_URL)
                                .header(
                                        headerRequestIdProperties.name(),
                                        requestId.toString()
                                )
                                .header(
                                        userContextProperties
                                                .userContextHeader(),
                                        SIGNED_USER_CONTEXT
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId")
                        .value(USER_ID))
                .andExpect(jsonPath("$.roles")
                        .isArray())
                .andExpect(jsonPath("$.roles")
                        .value(
                                org.hamcrest.Matchers.containsInAnyOrder(
                                        Role.ROLE_PATIENT.name(),
                                        Role.ROLE_ADMIN.name()
                                )
                        ));

        verify(
                userContextVerifier,
                times(1)
        ).verifyAndGetClaims(
                SIGNED_USER_CONTEXT
        );

        verify(
                userAuthInfoClient,
                times(1)
        ).getUserAuthInfo(USER_ID);
    }

    @Test
    void secondRequest_ShouldUseRedis_AndNotCallFeignAgain()
            throws Exception {

        UUID firstRequestId = UUID.randomUUID();
        UUID secondRequestId = UUID.randomUUID();

        saveRequestIdInRedis(firstRequestId);
        saveRequestIdInRedis(secondRequestId);

        Claims firstClaims = createClaims(
                firstRequestId,
                List.of(Role.ROLE_PATIENT.name())
        );

        Claims secondClaims = createClaims(
                secondRequestId,
                List.of(Role.ROLE_PATIENT.name())
        );

        UserAuthInfoDto authInfo =
                new UserAuthInfoDto(
                        USER_ID,
                        Set.of(Role.ROLE_PATIENT)
                );

        when(userContextVerifier.verifyAndGetClaims(
                SIGNED_USER_CONTEXT
        ))
                .thenReturn(firstClaims)
                .thenReturn(secondClaims);

        when(userAuthInfoClient.getUserAuthInfo(USER_ID))
                .thenReturn(authInfo);

        mockMvc.perform(
                        get(TEST_URL)
                                .header(
                                        headerRequestIdProperties.name(),
                                        firstRequestId.toString()
                                )
                                .header(
                                        userContextProperties
                                                .userContextHeader(),
                                        SIGNED_USER_CONTEXT
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(TEST_URL)
                                .header(
                                        headerRequestIdProperties.name(),
                                        secondRequestId.toString()
                                )
                                .header(
                                        userContextProperties
                                                .userContextHeader(),
                                        SIGNED_USER_CONTEXT
                                )
                )
                .andExpect(status().isOk());

        verify(
                userAuthInfoClient,
                times(1)
        ).getUserAuthInfo(USER_ID);
    }

    @Test
    void request_ShouldReturn400_WhenRequestIdHeaderIsMissing()
            throws Exception {

        mockMvc.perform(
                        get(TEST_URL)
                                .header(
                                        userContextProperties
                                                .userContextHeader(),
                                        SIGNED_USER_CONTEXT
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Header "
                                        + headerRequestIdProperties.name()
                                        + " is required"
                        ));

        verify(
                userContextVerifier,
                times(0)
        ).verifyAndGetClaims(
                SIGNED_USER_CONTEXT
        );

        verify(
                userAuthInfoClient,
                times(0)
        ).getUserAuthInfo(USER_ID);
    }

    @Test
    void request_ShouldReturn401_WhenSignedUserContextIsMissing()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        saveRequestIdInRedis(requestId);

        mockMvc.perform(
                        get(TEST_URL)
                                .header(
                                        headerRequestIdProperties.name(),
                                        requestId.toString()
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication failed"));

        verify(
                userAuthInfoClient,
                times(0)
        ).getUserAuthInfo(USER_ID);
    }

    @Test
    void request_ShouldReturn401_WhenUserContextSignatureIsInvalid()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        saveRequestIdInRedis(requestId);

        when(userContextVerifier.verifyAndGetClaims(
                SIGNED_USER_CONTEXT
        )).thenThrow(
                new SecurityException(
                        "Invalid signature"
                )
        );

        mockMvc.perform(
                        get(TEST_URL)
                                .header(
                                        headerRequestIdProperties.name(),
                                        requestId.toString()
                                )
                                .header(
                                        userContextProperties
                                                .userContextHeader(),
                                        SIGNED_USER_CONTEXT
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401));

        verify(
                userAuthInfoClient,
                times(0)
        ).getUserAuthInfo(USER_ID);
    }

    @Test
    void request_ShouldReturn401_WhenRequestIdDoesNotMatchSignedContext()
            throws Exception {

        UUID headerRequestId = UUID.randomUUID();
        UUID signedContextRequestId = UUID.randomUUID();

        saveRequestIdInRedis(headerRequestId);

        Claims claims = createClaims(
                signedContextRequestId,
                List.of(Role.ROLE_PATIENT.name())
        );

        when(userContextVerifier.verifyAndGetClaims(
                SIGNED_USER_CONTEXT
        )).thenReturn(claims);

        mockMvc.perform(
                        get(TEST_URL)
                                .header(
                                        headerRequestIdProperties.name(),
                                        headerRequestId.toString()
                                )
                                .header(
                                        userContextProperties
                                                .userContextHeader(),
                                        SIGNED_USER_CONTEXT
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401));

        verify(
                userAuthInfoClient,
                times(0)
        ).getUserAuthInfo(USER_ID);
    }

    @Test
    void request_ShouldReturn401_WhenRolesDoNotMatch()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        saveRequestIdInRedis(requestId);

        Claims claims = createClaims(
                requestId,
                List.of(Role.ROLE_ADMIN.name())
        );

        UserAuthInfoDto authInfo =
                new UserAuthInfoDto(
                        USER_ID,
                        Set.of(Role.ROLE_PATIENT)
                );

        when(userContextVerifier.verifyAndGetClaims(
                SIGNED_USER_CONTEXT
        )).thenReturn(claims);

        when(userAuthInfoClient.getUserAuthInfo(USER_ID))
                .thenReturn(authInfo);

        mockMvc.perform(
                        get(TEST_URL)
                                .header(
                                        headerRequestIdProperties.name(),
                                        requestId.toString()
                                )
                                .header(
                                        userContextProperties
                                                .userContextHeader(),
                                        SIGNED_USER_CONTEXT
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401));
    }

    private void saveRequestIdInRedis(
            UUID requestId
    ) {
        redisTemplate.opsForValue().set(
                requestIdProperties.prefix() + requestId,
                requestIdProperties.value(),
                requestIdProperties.ttl()
        );
    }

    private Claims createClaims(
            UUID requestId,
            List<String> roles
    ) {
        Claims claims =
                io.jsonwebtoken.Jwts.claims()
                        .subject(USER_ID_AS_STRING)
                        .add(
                                "rid",
                                requestId.toString()
                        )
                        .add(
                                "ver",
                                "1"
                        )
                        .add(
                                "roles",
                                roles
                        )
                        .build();

        return claims;
    }

    @TestConfiguration
    static class TestControllerConfiguration {

        @Bean
        SecurityTestController securityTestController() {
            return new SecurityTestController();
        }
    }

    @RestController
    static class SecurityTestController {

        @GetMapping(TEST_URL)
        Map<String, Object> currentUser(
                Authentication authentication
        ) {
            UserAuthInfoDto principal =
                    (UserAuthInfoDto)
                            authentication.getPrincipal();

            List<String> roles =
                    authentication.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();

            return Map.of(
                    "userId",
                    principal.userId(),
                    "roles",
                    roles
            );
        }
    }
}
