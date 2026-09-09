package com.healthcare.billing.model.entity.invoice.transition_rule;

import com.healthcare.billing.exception.InvoiceStateMachineException;
import com.healthcare.billing.model.entity.invoice.InvoiceIssueAction;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.validation.InvoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceTransitionRuleProvider {

    private final InvoiceValidator invoiceValidator;
    private final InvoiceIssueAction invoiceIssueAction;

    public InvoiceTransitionRule getEventRule(InvoiceEvent event) {

        if (event == null) {
            throw new InvoiceStateMachineException("Invoice event must not be null");
        }

        return switch (event) {

            case ISSUE -> new InvoiceTransitionRule(
                    invoiceValidator::validateForIssue,
                    invoiceIssueAction::execute
            );

            case PAY -> new InvoiceTransitionRule(
                    invoiceValidator::validateForPay,
                    inv -> {
                    });

            case CANCEL -> InvoiceTransitionRule.empty();
        };
    }
}
