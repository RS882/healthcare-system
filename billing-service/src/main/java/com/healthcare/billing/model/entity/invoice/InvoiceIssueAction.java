package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.config.propertie.BillingProperties;
import com.healthcare.billing.exception.InvoiceNumberGenerationException;
import com.healthcare.billing.generator.InvoiceNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InvoiceIssueAction {

    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final BillingProperties billingProperties;
    private final Clock clock;

    public void execute(Invoice invoice) {
        LocalDate issuedDate = LocalDate.now(clock);

        LocalDate dueDate = issuedDate.plusDays(billingProperties.paymentTermDays());

        String invoiceNumber = invoiceNumberGenerator.nextNumber();

        if (!StringUtils.hasText(invoiceNumber)) {
            throw new InvoiceNumberGenerationException("Generated invoice number must not be null or blank");
        }

        invoice.applyIssueData(
                invoiceNumber.strip(),
                issuedDate,
                dueDate
        );
    }
}
