package com.healthcare.user_service.security.internal_request.validator;

import com.healthcare.user_service.exception_handler.exception.InternalRequestGrantInvalidException;
import com.healthcare.user_service.security.internal_request.dto.InternalRequestGrant;
import com.healthcare.user_service.security.internal_request.consumer.interfaces.InternalRequestGrantValidator;
import com.healthcare.user_service.security.internal_request.properties.InternalRequestConsumerProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DefaultInternalRequestGrantValidator
        implements InternalRequestGrantValidator {

    private final InternalRequestConsumerProperties props;

    @Override
    public void validate(
            InternalRequestGrant grant,
            HttpServletRequest request
    ) {
        if (grant == null
                || grant.issuer() == null
                || !StringUtils.hasText(grant.issuer().serviceName())
                || !StringUtils.hasText(grant.target())
                || !StringUtils.hasText(grant.method())
                || !StringUtils.hasText(grant.path())) {

            throw invalid("Internal request grant is invalid");
        }

        validateTarget(grant);
        validateMethod(grant, request);
        validatePath(grant, request);
        validateIssuer(grant);
    }

    private void validateTarget(
            InternalRequestGrant grant
    ) {
        if (!props.serviceName().equals(grant.target())) {
            throw invalid("Internal request target mismatch");
        }
    }

    private void validateMethod(
            InternalRequestGrant grant,
            HttpServletRequest request
    ) {
        if (!request.getMethod().equalsIgnoreCase(grant.method())) {
            throw invalid("Internal request HTTP method mismatch");
        }
    }

    private void validatePath(
            InternalRequestGrant grant,
            HttpServletRequest request
    ) {
        String requestPath = request.getRequestURI();

        if (!grant.path().equals(requestPath)) {
            throw invalid( "Internal request path mismatch");
        }
    }

    private void validateIssuer(
            InternalRequestGrant grant
    ) {
        String issuer = grant.issuer().serviceName();

        if (!StringUtils.hasText(issuer)) {
            throw invalid(
                    "Internal request issuer is missing"
            );
        }

        if (!props.allowedIssuers().contains(issuer)) {
            throw invalid(
                    "Internal request issuer is not allowed: " + issuer
            );
        }
    }

    private InternalRequestGrantInvalidException invalid(String message) {
        return new InternalRequestGrantInvalidException(message);
    }
}
