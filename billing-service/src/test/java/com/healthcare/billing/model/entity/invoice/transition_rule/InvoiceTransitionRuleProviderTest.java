package com.healthcare.billing.model.entity.invoice.transition_rule;

import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.entity.invoice.InvoiceIssueAction;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.validation.InvoiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class InvoiceTransitionRuleProviderTest {

    private InvoiceValidator validator;
    private InvoiceIssueAction issueAction;

    private InvoiceTransitionRuleProvider ruleProvider;

    private Invoice invoice;

    @BeforeEach
    void setUp() {

        validator =
                mock(InvoiceValidator.class);

        issueAction =
                mock(InvoiceIssueAction.class);

        ruleProvider =
                new InvoiceTransitionRuleProvider(
                        validator,
                        issueAction
                );

        invoice =
                Invoice.builder()
                        .build();
    }

    @Test
    void shouldProvideIssueRule() {

        InvoiceTransitionRule rule =
                ruleProvider.getEventRule(
                        InvoiceEvent.ISSUE
                );

        rule.guard().accept(invoice);
        rule.action().accept(invoice);

        verify(validator).validateForIssue(invoice);

        verify(issueAction)
                .execute(invoice);
    }

    @Test
    void shouldProvidePayRule() {

        InvoiceTransitionRule rule =
                ruleProvider.getEventRule(
                        InvoiceEvent.PAY
                );

        rule.guard().accept(invoice);
        rule.action().accept(invoice);

        verify(validator)
                .validateForPay(invoice);

        verifyNoInteractions(
                issueAction
        );
    }

    @Test
    void shouldProvideEmptyCancelRule() {

        InvoiceTransitionRule rule =
                ruleProvider.getEventRule(
                        InvoiceEvent.CANCEL
                );

        rule.guard().accept(invoice);
        rule.action().accept(invoice);

        verifyNoInteractions(
                validator,
                issueAction
        );
    }
}