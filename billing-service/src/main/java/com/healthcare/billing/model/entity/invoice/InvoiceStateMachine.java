package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.entity.invoice.transition_rule.InvoiceTransitionRule;
import com.healthcare.billing.model.entity.invoice.transition_rule.InvoiceTransitionRuleProvider;
import com.healthcare.billing.validation.InvoiceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvoiceStateMachine {

    private final InvoiceTransitionRuleProvider ruleProvider;
    private final InvoiceTransitionResolver transitionResolver;
    private final InvoiceValidator validator;

    public void changeState(
            Invoice invoice,
            InvoiceEvent event
    ) {

        validator.validateInvoice(invoice);

        InvoiceStatus nextState = transitionResolver.resolve(invoice.getStatus(), event);

        InvoiceTransitionRule rule = ruleProvider.getEventRule(event);

        rule.guard().accept(invoice);

        rule.action().accept(invoice);

        invoice.applyStatus(nextState);
    }
}