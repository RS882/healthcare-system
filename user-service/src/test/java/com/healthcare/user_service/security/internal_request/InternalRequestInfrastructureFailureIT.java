package com.healthcare.user_service.security.internal_request;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.config.AbstractKafkaRedisMsqlTestContainer;
import com.healthcare.user_service.exception_handler.exception.InternalRequestAuthenticationServiceException;
import com.healthcare.user_service.model.dto.request.UserLookupDto;
import com.healthcare.user_service.security.internal_request.interfaces.InternalRequestGrantConsumer;
import com.healthcare.user_service.security.internal_request.properties.InternalRequestConsumerProperties;
import com.healthcare.user_service.service.interfacies.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.healthcare.user_service.controller.API.ApiPaths.INTERNAL_LOOKUP_URL;
import static org.mockito.ArgumentMatchers.any;
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
        "spring.task.scheduling.enabled=false",
        "spring.cloud.config.enabled=false",

        "internal-request.allowed-issuers[0]=auth-service"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Internal request infrastructure failure integration tests")
class InternalRequestInfrastructureFailureIT
        extends AbstractKafkaRedisMsqlTestContainer {

    private static final String EMAIL =
            "internal-test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternalRequestConsumerProperties internalRequestProperties;

    @MockitoBean
    private InternalRequestGrantConsumer grantConsumer;

    @MockitoBean
    private UserService userService;

    @Test
    void should_return_503_when_internal_request_authentication_service_is_unavailable()
            throws Exception {

        UUID internalRequestId =
                UUID.randomUUID();

        when(grantConsumer.consume(any(UUID.class)))
                .thenThrow(
                        new InternalRequestAuthenticationServiceException(
                                "Internal request authentication service is unavailable",
                                new RuntimeException("Redis unavailable")
                        )
                );

        UserLookupDto request =
                new UserLookupDto(EMAIL);

        mockMvc.perform(
                        post(INTERNAL_LOOKUP_URL)
                                .header(
                                        internalRequestProperties.headerName(),
                                        internalRequestId.toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isServiceUnavailable())
                .andExpect(
                        jsonPath("$.status")
                                .value(503)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Service Unavailable")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(INTERNAL_LOOKUP_URL)
                );

        verify(grantConsumer)
                .consume(internalRequestId);

        verifyNoInteractions(userService);
    }
}
