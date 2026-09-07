package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import com.healthcare.billing.model.entity.invoice.transition_rule.InvoiceTransitionRule;
import com.healthcare.billing.model.entity.invoice.transition_rule.InvoiceTransitionRuleProvider;
import com.healthcare.billing.validation.InvoiceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InvoiceStateMachineTest {

    private InvoiceTransitionRuleProvider ruleProvider;
    private InvoiceTransitionResolver transitionResolver;
    private InvoiceValidator validator;

    private InvoiceStateMachine stateMachine;

    @BeforeEach
    void setUp() {

        ruleProvider = mock(InvoiceTransitionRuleProvider.class);

        transitionResolver = mock(InvoiceTransitionResolver.class);

        validator = mock(InvoiceValidator.class);

        stateMachine = new InvoiceStateMachine(
                ruleProvider,
                transitionResolver,
                validator
        );
    }

    @Test
    void shouldChangeInvoiceState() {

        Invoice invoice = createInvoice();

        Consumer<Invoice> guard = mock(Consumer.class);

        Consumer<Invoice> action = mock(Consumer.class);

        InvoiceTransitionRule rule = new InvoiceTransitionRule(guard, action);

        when(
                transitionResolver.resolve(
                        InvoiceStatus.DRAFT,
                        InvoiceEvent.ISSUE
                )
        ).thenReturn(
                InvoiceStatus.ISSUED
        );

        when(ruleProvider.getEventRule(InvoiceEvent.ISSUE)).thenReturn(rule);

        stateMachine.changeState(invoice, InvoiceEvent.ISSUE);

        verify(validator).validateInvoice(invoice);

        verify(transitionResolver).resolve(
                InvoiceStatus.DRAFT,
                InvoiceEvent.ISSUE
        );

        verify(ruleProvider).getEventRule(InvoiceEvent.ISSUE);

        verify(guard).accept(invoice);

        verify(action).accept(invoice);

        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
    }

    @Test
    void shouldRejectNullInvoiceBeforeResolvingTransition() {

        doThrow(new IllegalArgumentException("Invoice must not be null"))
                .when(validator).validateInvoice(null);

        assertThrows(IllegalArgumentException.class,
                () -> stateMachine.changeState(null, InvoiceEvent.ISSUE)
        );

        verify(validator).validateInvoice(null);

        verifyNoInteractions(transitionResolver, ruleProvider);
    }

    @Test
    void shouldNotExecuteRuleWhenTransitionIsNotAllowed() {

        Invoice invoice =  createInvoice();

        when(transitionResolver.resolve(InvoiceStatus.DRAFT, InvoiceEvent.PAY))
                .thenThrow(new IllegalStateException("Transition is not allowed"));

        assertThrows(IllegalStateException.class,
                () -> stateMachine.changeState(invoice, InvoiceEvent.PAY));

        verify(validator).validateInvoice(invoice);

        verify(transitionResolver).resolve(InvoiceStatus.DRAFT, InvoiceEvent.PAY);

        verifyNoInteractions(ruleProvider);

        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
    }

    @Test
    void shouldNotChangeStateWhenGuardFails() {

        Invoice invoice =  createInvoice();

        Consumer<Invoice> guard = mock(Consumer.class);

        Consumer<Invoice> action = mock(Consumer.class);

        InvoiceTransitionRule rule = new InvoiceTransitionRule(guard, action);

        when(transitionResolver.resolve(InvoiceStatus.DRAFT, InvoiceEvent.ISSUE))
                .thenReturn(InvoiceStatus.ISSUED);

        when(ruleProvider.getEventRule(InvoiceEvent.ISSUE)).thenReturn(rule);

        doThrow(new IllegalStateException("Invoice is invalid"))
                .when(guard).accept(invoice);

        assertThrows(IllegalStateException.class,
                () -> stateMachine.changeState(invoice, InvoiceEvent.ISSUE));

        verify(validator).validateInvoice(invoice);

        verify(guard).accept(invoice);

        verify(action, never()).accept(invoice);

        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
    }

    @Test
    void shouldNotChangeStateWhenActionFails() {

        Invoice invoice =  createInvoice();

        Consumer<Invoice> guard = mock(Consumer.class);

        Consumer<Invoice> action = mock(Consumer.class);

        InvoiceTransitionRule rule = new InvoiceTransitionRule(guard, action);

        when(transitionResolver.resolve(InvoiceStatus.DRAFT, InvoiceEvent.ISSUE))
                .thenReturn(InvoiceStatus.ISSUED);

        when(ruleProvider.getEventRule(InvoiceEvent.ISSUE)).thenReturn(rule);

        doThrow(new IllegalStateException("Action failed")).when(action).accept(invoice);

        assertThrows(IllegalStateException.class, () -> stateMachine.changeState(invoice, InvoiceEvent.ISSUE));

        verify(validator).validateInvoice(invoice);

        verify(guard).accept(invoice);

        verify(action).accept(invoice);

        assertEquals(InvoiceStatus.DRAFT, invoice.getStatus());
    }

    @Test
    void shouldExecuteTransitionStepsInCorrectOrder() {

        Invoice invoice =  createInvoice();

        Consumer<Invoice> guard = mock(Consumer.class);

        Consumer<Invoice> action = mock(Consumer.class);

        InvoiceTransitionRule rule = new InvoiceTransitionRule(guard, action);

        when(transitionResolver.resolve(InvoiceStatus.DRAFT, InvoiceEvent.ISSUE))
                .thenReturn(InvoiceStatus.ISSUED);

        when(ruleProvider.getEventRule(InvoiceEvent.ISSUE)).thenReturn(rule);

        stateMachine.changeState(invoice, InvoiceEvent.ISSUE);

        org.mockito.InOrder inOrder = inOrder(
                validator,
                transitionResolver,
                ruleProvider,
                guard,
                action
        );

        inOrder.verify(validator).validateInvoice(invoice);

        inOrder.verify(transitionResolver).resolve(InvoiceStatus.DRAFT, InvoiceEvent.ISSUE);

        inOrder.verify(ruleProvider).getEventRule(InvoiceEvent.ISSUE);

        inOrder.verify(guard).accept(invoice);

        inOrder.verify(action).accept(invoice);

        assertEquals(InvoiceStatus.ISSUED, invoice.getStatus());
    }

    private Invoice createInvoice() {
        return Invoice.builder().build();
    }
}