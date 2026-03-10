package com.healthcare.user_service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.user_service.config.properties.HeaderRequestIdProperties;
import com.healthcare.user_service.exception_handler.dto.ErrorResponse;
import com.healthcare.user_service.service.interfacies.RequestIdService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.healthcare.user_service.filter.security.constant.AttrNames.ATTR_REQUEST_ID;
import static com.healthcare.user_service.support.TestDataFactory.requestId;
import static com.healthcare.user_service.support.TestGatewayConstants.HEADER_REQUEST_ID;
import static com.healthcare.user_service.support.TestGatewayConstants.TEST_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = RequestIdFilterWebMvcIT.TestController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc
@EnableConfigurationProperties(HeaderRequestIdProperties.class)
@Import({
        RequestIdFilterWebMvcIT.FilterTestConfig.class,
        RequestIdFilterWebMvcIT.TestController.class,
})
@TestPropertySource(properties = {
        "header-request-id.name=" + HEADER_REQUEST_ID,
        "spring.cloud.config.enabled=false",
        "user-context-filter.enabled=false",
        "auth-filter.enabled=false"
})
class RequestIdFilterWebMvcIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private RequestIdService requestIdService;

    @RestController
    public static class TestController {
        @GetMapping(value = TEST_URI, produces = MediaType.TEXT_PLAIN_VALUE)
        public String getRequestId(HttpServletRequest request) {
            Object v = request.getAttribute(ATTR_REQUEST_ID);
            return v == null ? "null" : v.toString();
        }
    }

    @TestConfiguration
    static class FilterTestConfig {

        @Bean
        RequestIdFilter requestIdFilter(
                HeaderRequestIdProperties props,
                RequestIdService requestIdService,
                org.springframework.web.servlet.HandlerExceptionResolver handlerExceptionResolver
        ) {
            return new RequestIdFilter(props, requestIdService, handlerExceptionResolver);
        }

        @Bean
        FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration(RequestIdFilter filter) {
            FilterRegistrationBean<RequestIdFilter> reg = new FilterRegistrationBean<>();
            reg.setFilter(filter);
            reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
            reg.addUrlPatterns("/*");
            return reg;
        }
    }

    @Test
    void shouldReturn400_whenHeaderMissing() throws Exception {
        MvcResult result = mockMvc.perform(get(TEST_URI))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        assertTrue(StringUtils.hasText(error.getMessage().toString()));
        assertEquals(error.getStatus(), status.value());
        assertEquals(error.getError(), status.getReasonPhrase());
        assertEquals(TEST_URI, error.getPath());
    }

    @Test
    void shouldReturn400_whenHeaderInvalidUuid() throws Exception {
        String invalid = "not-uuid";
        Mockito.when(requestIdService.isRequestIdValid(invalid)).thenReturn(false);

        MvcResult result = mockMvc.perform(get(TEST_URI)
                        .header(HEADER_REQUEST_ID, invalid))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = mapper.readValue(responseBody, ErrorResponse.class);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        assertTrue(StringUtils.hasText(error.getMessage().toString()));
        assertEquals(error.getStatus(), status.value());
        assertEquals(error.getError(), status.getReasonPhrase());
        assertEquals(TEST_URI, error.getPath());

        verify(requestIdService).isRequestIdValid(invalid);
    }

    @Test
    void shouldPass_whenHeaderValid_andAttributeSet() throws Exception {
        String rid = requestId().toString();
        Mockito.when(requestIdService.isRequestIdValid(rid)).thenReturn(true);

        mockMvc.perform(get(TEST_URI).header(HEADER_REQUEST_ID, rid))
                .andExpect(status().isOk())
                .andExpect(content().string(rid));

        verify(requestIdService).isRequestIdValid(rid);
    }
}