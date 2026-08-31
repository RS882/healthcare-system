package com.healthcare.aiservice.security.internal_request;

import com.healthcare.aiservice.security.internal_request.interfaces.InternalRequestGrantIssuer;
import com.healthcare.aiservice.security.internal_request.properties.InternalRequestIssuerProperties;
import com.healthcare.aiservice.security.internal_request.properties.UserServiceInternalClientProperties;
import com.healthcare.aiservice.security.properties.HeaderRequestIdProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static com.healthcare.aiservice.security.filter.security.constant.AttrNames.ATTR_REQUEST_ID;

@RequiredArgsConstructor
public class InternalRequestFeignInterceptor
        implements RequestInterceptor {

    private final InternalRequestGrantIssuer grantIssuer;
    private final InternalRequestIssuerProperties issuerProperties;
    private final UserServiceInternalClientProperties clientProperties;
    private final HeaderRequestIdProperties requestIdProperties;

    @Override
    public void apply(RequestTemplate template) {

        HttpMethod method =
                HttpMethod.valueOf(template.method());

        String path = buildRequestPath(
                clientProperties.basePath(),
                template.path()
        );

        UUID internalRequestId =
                grantIssuer.issue(
                        clientProperties.targetService(),
                        method,
                        path
                );

        template.header(
                issuerProperties.headerName(),
                internalRequestId.toString()
        );

        propagateRequestId(template);
    }

    private void propagateRequestId(
            RequestTemplate template
    ) {
        HttpServletRequest request =
                getCurrentHttpRequest();

        if (request == null) {
            return;
        }

        Object requestIdAttribute =
                request.getAttribute(
                        ATTR_REQUEST_ID
                );

        if (!(requestIdAttribute instanceof String requestId)
                || !StringUtils.hasText(requestId)) {
            return;
        }

        template.header(
                requestIdProperties.name(),
                requestId
        );
    }

    private HttpServletRequest getCurrentHttpRequest() {

        RequestAttributes requestAttributes =
                RequestContextHolder.getRequestAttributes();

        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        return servletRequestAttributes.getRequest();
    }

    private String buildRequestPath(
            String basePath,
            String path
    ) {
        String normalizedBasePath =
                basePath.endsWith("/")
                        ? basePath.substring(
                        0,
                        basePath.length() - 1
                )
                        : basePath;

        String normalizedPath =
                path.startsWith("/")
                        ? path
                        : "/" + path;

        return normalizedBasePath + normalizedPath;
    }
}