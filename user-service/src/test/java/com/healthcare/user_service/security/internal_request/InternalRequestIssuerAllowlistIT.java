package com.healthcare.user_service.security.internal_request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.config.AbstractKafkaRedisMsqlTestContainer;
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

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static com.healthcare.user_service.controller.API.ApiPaths.INTERNAL_LOOKUP_URL;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        "internal-request.allowed-issuers[0]=auth-service"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Internal request issuer allowlist integration tests")
class InternalRequestIssuerAllowlistIT
        extends AbstractKafkaRedisMsqlTestContainer {

    private static final Duration TEST_GRANT_TTL =
            Duration.ofSeconds(30);

    private static final String EMAIL =
            "internal-test@example.com";

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
    void should_return_401_when_internal_request_issuer_is_not_allowed()
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
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    private ResultActions performInternalLookup(
            UUID internalRequestId
    ) throws Exception {

        UserLookupDto request =
                new UserLookupDto(EMAIL);

        return mockMvc.perform(
                post(INTERNAL_LOOKUP_URL)
                        .header(
                                internalRequestProperties.headerName(),
                                internalRequestId.toString()
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
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

        redisTemplate.opsForValue().set(
                redisKey,
                serializedGrant,
                TEST_GRANT_TTL
        );
    }
}