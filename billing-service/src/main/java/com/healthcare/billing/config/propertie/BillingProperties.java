package com.healthcare.billing.config.propertie;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "billing")
public record BillingProperties(

        @NotBlank(message = "Currency field must not be blank.")
        String currency,

        @Min(
                value = 1,
                message = "Invoice payment term must be at least 1 day."
        )
        int paymentTermDays
) {
}

