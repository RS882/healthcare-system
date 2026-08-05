package com.healthcare.aiservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.cache.CacheNames;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionRequest;
import com.healthcare.aiservice.common.medical_extraction.dto.MedicalInfoExtractionResponse;
import com.healthcare.aiservice.common.medical_extraction.service.MedicalInfoExtractionService;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.healthcare.aiservice.common.medical_extraction.controller.API
        .MedicalInfoExtractionApiPaths.EXTRACT_MEDICAL_INFO_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Security authentication integration tests: ")
class SecurityAuthenticationIntegrationTest
        extends AbstractMongoRedisIntegrationTest {

    private static final long USER_ID = 42L;

    private static final String USER_ID_AS_STRING = "42";

    private static final String SIGNED_USER_CONTEXT =
            "signed-user-context-token";

    private static final String MEDICAL_NOTE =
            "Patient reports fever and dry cough.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private HeaderRequestIdProperties headerRequestIdProperties;

    @Autowired
    private UserContextProperties userContextProperties;

    @Autowired
    private RequestIdProperties requestIdProperties;

    @MockitoBean
    private UserContextVerifier userContextVerifier;

    @MockitoBean
    private UserAuthInfoClient userAuthInfoClient;

    @MockitoBean
    private MedicalInfoExtractionService medicalInfoExtractionService;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache(
                CacheNames.USER_AUTH_INFO
        );

        assertThat(cache).isNotNull();

        cache.clear();

        reset(
                userContextVerifier,
                userAuthInfoClient,
                medicalInfoExtractionService
        );
    }

    @Test
    void request_ShouldAuthenticateUser_AndReachProtectedController()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        prepareAuthenticatedRequest(
                requestId,
                Set.of(
                        Role.ROLE_PATIENT,
                        Role.ROLE_ADMIN
                )
        );

        when(medicalInfoExtractionService.extract(
                any(MedicalInfoExtractionRequest.class)
        )).thenReturn(createMedicalResponse());

        mockMvc.perform(
                        authenticatedRequest(requestId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symptoms[0]")
                        .value("Fever"))
                .andExpect(jsonPath("$.symptoms[1]")
                        .value("Dry cough"));

        verify(userContextVerifier, times(1))
                .verifyAndGetClaims(SIGNED_USER_CONTEXT);

        verify(userAuthInfoClient, times(1))
                .getUserAuthInfo(USER_ID);

        verify(medicalInfoExtractionService, times(1))
                .extract(any(MedicalInfoExtractionRequest.class));
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

        when(medicalInfoExtractionService.extract(
                any(MedicalInfoExtractionRequest.class)
        )).thenReturn(createMedicalResponse());

        mockMvc.perform(authenticatedRequest(firstRequestId))
                .andExpect(status().isOk());

        mockMvc.perform(authenticatedRequest(secondRequestId))
                .andExpect(status().isOk());

        verify(userAuthInfoClient, times(1))
                .getUserAuthInfo(USER_ID);

        verify(medicalInfoExtractionService, times(2))
                .extract(any(MedicalInfoExtractionRequest.class));
    }

    @Test
    void request_ShouldReturn400_WhenRequestIdHeaderIsMissing()
            throws Exception {

        mockMvc.perform(
                        post(EXTRACT_MEDICAL_INFO_URL)
                                .header(
                                        userContextProperties
                                                .userContextHeader(),
                                        SIGNED_USER_CONTEXT
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Header "
                                        + headerRequestIdProperties.name()
                                        + " is required"
                        ));

        verifyNoInteractions(
                userContextVerifier,
                userAuthInfoClient,
                medicalInfoExtractionService
        );
    }

    @Test
    void request_ShouldReturn401_WhenSignedUserContextIsMissing()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        saveRequestIdInRedis(requestId);

        mockMvc.perform(
                        post(EXTRACT_MEDICAL_INFO_URL)
                                .header(
                                        headerRequestIdProperties.name(),
                                        requestId.toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody())
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Authentication failed"));

        verifyNoInteractions(
                userAuthInfoClient,
                medicalInfoExtractionService
        );
    }

    @Test
    void request_ShouldReturn401_WhenUserContextSignatureIsInvalid()
            throws Exception {

        UUID requestId = UUID.randomUUID();

        saveRequestIdInRedis(requestId);

        when(userContextVerifier.verifyAndGetClaims(
                SIGNED_USER_CONTEXT
        )).thenThrow(
                new SecurityException("Invalid signature")
        );

        mockMvc.perform(authenticatedRequest(requestId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(
                userAuthInfoClient,
                medicalInfoExtractionService
        );
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

        mockMvc.perform(authenticatedRequest(headerRequestId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(
                userAuthInfoClient,
                medicalInfoExtractionService
        );
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

        mockMvc.perform(authenticatedRequest(requestId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(medicalInfoExtractionService);
    }

    private void prepareAuthenticatedRequest(
            UUID requestId,
            Set<Role> roles
    ) {
        saveRequestIdInRedis(requestId);

        Claims claims = createClaims(
                requestId,
                roles.stream()
                        .map(Role::name)
                        .toList()
        );

        UserAuthInfoDto authInfo =
                new UserAuthInfoDto(
                        USER_ID,
                        roles
                );

        when(userContextVerifier.verifyAndGetClaims(
                SIGNED_USER_CONTEXT
        )).thenReturn(claims);

        when(userAuthInfoClient.getUserAuthInfo(USER_ID))
                .thenReturn(authInfo);
    }

    private org.springframework.test.web.servlet.request
            .MockHttpServletRequestBuilder authenticatedRequest(
            UUID requestId
    ) throws Exception {
        return post(EXTRACT_MEDICAL_INFO_URL)
                .header(
                        headerRequestIdProperties.name(),
                        requestId.toString()
                )
                .header(
                        userContextProperties.userContextHeader(),
                        SIGNED_USER_CONTEXT
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody());
    }

    private String requestBody() throws Exception {
        MedicalInfoExtractionRequest request =
                new MedicalInfoExtractionRequest(
                        MEDICAL_NOTE
                );

        return objectMapper.writeValueAsString(request);
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
        return io.jsonwebtoken.Jwts.claims()
                .subject(USER_ID_AS_STRING)
                .add("rid", requestId.toString())
                .add("ver", "1")
                .add("roles", roles)
                .build();
    }

    private MedicalInfoExtractionResponse createMedicalResponse() {
        return new MedicalInfoExtractionResponse(
                List.of("Fever", "Dry cough"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}