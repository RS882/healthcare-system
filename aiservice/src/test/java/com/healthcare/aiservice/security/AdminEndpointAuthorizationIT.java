package com.healthcare.aiservice.security;

import com.healthcare.aiservice.cache.CacheNames;
import com.healthcare.aiservice.common.statistics.dto.AiStatisticsResponse;
import com.healthcare.aiservice.common.statistics.service.interfaces.AiStatisticService;
import com.healthcare.aiservice.config.AbstractMongoRedisIntegrationTest;
import com.healthcare.aiservice.security.constant.Role;
import com.healthcare.aiservice.security.dto.UserAuthInfoDto;
import com.healthcare.aiservice.security.feign_client.UserAuthInfoClient;
import com.healthcare.aiservice.security.filter.security.interfaces.UserContextVerifier;
import com.healthcare.aiservice.security.properties.HeaderRequestIdProperties;
import com.healthcare.aiservice.security.properties.RequestIdProperties;
import com.healthcare.aiservice.security.properties.UserContextProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.healthcare.aiservice.common.prompt.controller.API.AiPromptApiPaths.*;
import static com.healthcare.aiservice.common.statistics.controller.API.AiStatisticsApiPaths.STATISTICS_ADMIN_URL;
import static jakarta.ws.rs.HttpMethod.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Admin endpoint authorization integration tests: ")
class AdminEndpointAuthorizationIT
        extends AbstractMongoRedisIntegrationTest {

    private static final long USER_ID = 42L;

    private static final String SIGNED_CONTEXT =
            "signed-user-context";

    private static final String PROMPT_ID =
            "prompt-id";

    private static final String CONCRETE_PROMPT_BY_ID_URL =
            PROMPT_BY_ID_URL.replace(
                    "{promptId}",
                    PROMPT_ID
            );

    private static final String CONCRETE_ACTIVATE_PROMPT_URL =
            ACTIVATE_PROMPT_URL.replace(
                    "{promptId}",
                    PROMPT_ID
            );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

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

    @MockitoBean
    private AiStatisticService statisticService;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache(
                CacheNames.USER_AUTH_INFO
        );

        assertThat(cache)
                .as("User authentication cache must be configured")
                .isNotNull();

        cache.clear();

        reset(
                userContextVerifier,
                userAuthInfoClient,
                statisticService
        );
    }

    @Test
    void statisticsEndpoint_ShouldReturn200_ForAdmin()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        prepareAuthenticatedRequest(
                requestId,
                Set.of(Role.ROLE_ADMIN)
        );

        when(statisticService.getStatistic())
                .thenReturn(emptyStatistics());

        mockMvc.perform(
                        authenticatedRequest(
                                HttpMethod.GET,
                                STATISTICS_ADMIN_URL,
                                requestId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests")
                        .value(0))
                .andExpect(jsonPath("$.successfulRequests")
                        .value(0))
                .andExpect(jsonPath("$.failedRequests")
                        .value(0))
                .andExpect(jsonPath("$.averageDurationMs")
                        .value(0));

        verify(statisticService)
                .getStatistic();
    }

    @ParameterizedTest(
            name = "{0} {1} should return 403 for non-admin"
    )
    @MethodSource("adminEndpoints")
    void adminEndpoints_ShouldReturn403_ForAuthenticatedNonAdmin(
            HttpMethod method,
            String url
    ) throws Exception {

        UUID requestId = UUID.randomUUID();

        prepareAuthenticatedRequest(
                requestId,
                Set.of(Role.ROLE_PATIENT)
        );

        mockMvc.perform(
                        authenticatedRequest(
                                method,
                                url,
                                requestId
                        )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status")
                        .value(403));

        /*
         * Authorization must stop the request before
         * the statistics controller invokes its service.
         */
        verifyNoInteractions(statisticService);
    }

    @Test
    void statisticsEndpoint_ShouldReturn401_WhenUserContextIsMissing()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        saveRequestId(requestId);

        mockMvc.perform(
                        get(STATISTICS_ADMIN_URL)
                                .header(
                                        headerRequestIdProperties.name(),
                                        requestId.toString()
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401));

        verifyNoInteractions(
                userAuthInfoClient,
                statisticService
        );
    }

    @Test
    void statisticsEndpoint_ShouldReturn400_WhenRequestIdIsMissing()
            throws Exception {

        mockMvc.perform(
                        get(STATISTICS_ADMIN_URL)
                                .header(
                                        userContextProperties
                                                .userContextHeader(),
                                        SIGNED_CONTEXT
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400));

        verifyNoInteractions(
                userContextVerifier,
                userAuthInfoClient,
                statisticService
        );
    }

    private static Stream<Arguments> adminEndpoints() {
        return Stream.of(
                Arguments.of(
                        HttpMethod.GET,
                        STATISTICS_ADMIN_URL
                ),
                Arguments.of(
                        HttpMethod.POST,
                        PROMPTS_URL
                ),
                Arguments.of(
                        HttpMethod.GET,
                        PROMPTS_URL
                ),
                Arguments.of(
                        HttpMethod.GET,
                        CONCRETE_PROMPT_BY_ID_URL
                ),
                Arguments.of(
                        HttpMethod.PATCH,
                        CONCRETE_ACTIVATE_PROMPT_URL
                ),
                Arguments.of(
                        HttpMethod.GET,
                        CURRENT_PROMPT_URL
                )
        );
    }

    private void prepareAuthenticatedRequest(
            UUID requestId,
            Set<Role> roles
    ) {
        saveRequestId(requestId);

        Claims claims = createClaims(
                requestId,
                roles
        );

        UserAuthInfoDto authInfo =
                new UserAuthInfoDto(
                        USER_ID,
                        roles
                );

        when(userContextVerifier.verifyAndGetClaims(
                SIGNED_CONTEXT
        )).thenReturn(claims);

        when(userAuthInfoClient.getUserAuthInfo(USER_ID))
                .thenReturn(authInfo);
    }

    private MockHttpServletRequestBuilder authenticatedRequest(
            HttpMethod method,
            String url,
            UUID requestId
    ) {
        return request(method, url)
                .header(
                        headerRequestIdProperties.name(),
                        requestId.toString()
                )
                .header(
                        userContextProperties.userContextHeader(),
                        SIGNED_CONTEXT
                );
    }

    private MockHttpServletRequestBuilder request(
            HttpMethod method,
            String url
    ) {
        return switch (method.name()) {
            case GET -> get(url);
            case POST -> post(url);
            case PATCH -> patch(url);
            case PUT -> put(url);
            case DELETE -> delete(url);

            default -> throw new IllegalArgumentException(
                    "Unsupported HTTP method: " + method
            );
        };
    }

    private void saveRequestId(
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
            Set<Role> roles
    ) {
        List<String> roleNames =
                roles.stream()
                        .map(Role::name)
                        .toList();

        return io.jsonwebtoken.Jwts.claims()
                .subject(String.valueOf(USER_ID))
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
                        roleNames
                )
                .build();
    }

    private AiStatisticsResponse emptyStatistics() {
        return AiStatisticsResponse.builder()
                .totalRequests(0L)
                .successfulRequests(0L)
                .failedRequests(0L)
                .averageDurationMs(0L)
                .requestsByFeature(List.of())
                .build();
    }
}