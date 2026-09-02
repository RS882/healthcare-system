package com.healthcare.billing.config.propertie;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "billing")
public record BillingProperties(

        @NotBlank( message = "Currency field must not be blank.")
        String currency
) {
}
