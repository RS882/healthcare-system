package com.healthcare.aiservice.security.feign_client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.aiservice.config.AbstractMongoRedisIntegrationTest;
import com.healthcare.aiservice.security.internal_request.properties.InternalRequestIssuerProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.healthcare.aiservice.security.filter.security.constant.AttrNames.ATTR_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("UserAuthInfoClient integration tests: ")
class UserAuthInfoClientIntegrationTest
        extends AbstractMongoRedisIntegrationTest {

    private static final long USER_ID = 42L;

    private static final String EXPECTED_PATH =
            "/api/v1/users/internal/42/auth-info";

    private static final String INTERNAL_REQUEST_HEADER =
            "X-Internal-Request-Id";

    private static final String REQUEST_ID_HEADER =
            "X-Request-Id";

    private static HttpServer httpServer;

    private static int port;

    private static final AtomicReference<String> receivedPath =
            new AtomicReference<>();

    private static final AtomicReference<String> receivedInternalRequestId =
            new AtomicReference<>();

    private static final AtomicReference<String> receivedRequestId =
            new AtomicReference<>();

    @Autowired
    private UserAuthInfoClient userAuthInfoClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private InternalRequestIssuerProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void startHttpServer() throws IOException {

        httpServer = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        port = httpServer.getAddress().getPort();

        httpServer.createContext(
                "/",
                UserAuthInfoClientIntegrationTest::handleRequest
        );

        httpServer.start();
    }

    @BeforeEach
    void setUp() {

        receivedPath.set(null);
        receivedInternalRequestId.set(null);
        receivedRequestId.set(null);
    }

    @AfterEach
    void tearDown() {

        RequestContextHolder.resetRequestAttributes();
    }

    @AfterAll
    static void stopHttpServer() {

        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @DynamicPropertySource
    static void clientProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.cloud.openfeign.client.config.userAuthInfoClient.url",
                () -> "http://localhost:" + port
        );
    }

    @Test
    void getUserAuthInfo_ShouldPropagateRequestId_SendInternalRequestId_AndStoreMatchingGrantInRedis()
            throws Exception {

        String requestId =
                UUID.randomUUID().toString();

        setCurrentRequestId(requestId);

        userAuthInfoClient.getUserAuthInfo(USER_ID);

        String internalRequestId =
                receivedInternalRequestId.get();

        assertThat(receivedPath.get())
                .isEqualTo(EXPECTED_PATH);

        assertThat(receivedRequestId.get())
                .isEqualTo(requestId);

        assertThat(internalRequestId)
                .isNotBlank();

        UUID.fromString(internalRequestId);

        String redisKey =
                properties.keyPrefix()
                        + internalRequestId;

        String storedGrant =
                redisTemplate.opsForValue()
                        .get(redisKey);

        assertThat(storedGrant)
                .isNotNull();

        JsonNode grant =
                objectMapper.readTree(storedGrant);

        assertThat(grant.get("issuer").asText())
                .isEqualTo("ai-service");

        assertThat(grant.get("target").asText())
                .isEqualTo("user-service");

        assertThat(grant.get("method").asText())
                .isEqualTo("GET");

        assertThat(grant.get("path").asText())
                .isEqualTo(EXPECTED_PATH);

        assertThat(grant.get("createdAt").asText())
                .isNotBlank();
    }

    private void setCurrentRequestId(
            String requestId
    ) {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setAttribute(
                ATTR_REQUEST_ID,
                requestId
        );

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );
    }

    private static void handleRequest(
            HttpExchange exchange
    ) throws IOException {

        receivedPath.set(
                exchange.getRequestURI()
                        .getPath()
        );

        receivedInternalRequestId.set(
                exchange.getRequestHeaders()
                        .getFirst(INTERNAL_REQUEST_HEADER)
        );

        receivedRequestId.set(
                exchange.getRequestHeaders()
                        .getFirst(REQUEST_ID_HEADER)
        );

        byte[] response = """
                {
                  "userId": 42,
                  "roles": []
                }
                """.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(
                200,
                response.length
        );

        exchange.getResponseBody()
                .write(response);

        exchange.close();
    }
}