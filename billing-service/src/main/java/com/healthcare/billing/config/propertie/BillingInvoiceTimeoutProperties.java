package com.healthcare.billing.config.propertie;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "billing.invoice")
public record BillingInvoiceTimeoutProperties(

        @Positive(message = "Invoice lock timeout must be positive")
        long lockTimeoutMs
) {
}
