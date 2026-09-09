package com.healthcare.billing.model.entity.invoice;

import com.healthcare.billing.exception.InvalidInvoiceTransitionException;
import com.healthcare.billing.exception.InvoiceStateMachineException;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceEvent;
import com.healthcare.billing.model.entity.invoice.enums.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceTransitionResolverTest {

    private InvoiceTransitionResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new InvoiceTransitionResolver();
    }

    @Test
    void shouldResolveDraftToIssued() {
        assertEquals(
                InvoiceStatus.ISSUED,
                resolver.resolve(InvoiceStatus.DRAFT, InvoiceEvent.ISSUE)
        );
    }

    @Test
    void shouldResolveDraftToCancelled() {
        assertEquals(
                InvoiceStatus.CANCELLED,
                resolver.resolve(InvoiceStatus.DRAFT, InvoiceEvent.CANCEL)
        );
    }

    @Test
    void shouldResolveIssuedToPaid() {
        assertEquals(
                InvoiceStatus.PAID,
                resolver.resolve(InvoiceStatus.ISSUED, InvoiceEvent.PAY)
        );
    }

    @Test
    void shouldResolveIssuedToCancelled() {
        assertEquals(
                InvoiceStatus.CANCELLED,
                resolver.resolve(InvoiceStatus.ISSUED, InvoiceEvent.CANCEL)
        );
    }

    @Test
    void shouldRejectNullCurrentState() {
        assertThrows(
                InvoiceStateMachineException.class,
                () -> resolver.resolve(null, InvoiceEvent.ISSUE)
        );
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(
                InvoiceStateMachineException.class,
                () -> resolver.resolve(InvoiceStatus.DRAFT, null)
        );
    }

    @Test
    void shouldRejectPaymentFromDraft() {
        assertForbidden(InvoiceStatus.DRAFT, InvoiceEvent.PAY);
    }

    @Test
    void shouldRejectIssueFromIssued() {
        assertForbidden(InvoiceStatus.ISSUED, InvoiceEvent.ISSUE);
    }

    @Test
    void shouldRejectIssueFromPaid() {
        assertForbidden(InvoiceStatus.PAID, InvoiceEvent.ISSUE);
    }

    @Test
    void shouldRejectPaymentFromPaid() {
        assertForbidden(InvoiceStatus.PAID, InvoiceEvent.PAY);
    }

    @Test
    void shouldRejectCancellationFromPaid() {
        assertForbidden(InvoiceStatus.PAID, InvoiceEvent.CANCEL);
    }

    @Test
    void shouldRejectIssueFromCancelled() {
        assertForbidden(InvoiceStatus.CANCELLED, InvoiceEvent.ISSUE);
    }

    @Test
    void shouldRejectPaymentFromCancelled() {
        assertForbidden(InvoiceStatus.CANCELLED, InvoiceEvent.PAY);
    }

    @Test
    void shouldRejectCancellationFromCancelled() {
        assertForbidden(InvoiceStatus.CANCELLED, InvoiceEvent.CANCEL);
    }

    private void assertForbidden(InvoiceStatus status, InvoiceEvent event) {
        assertThrows(
                InvalidInvoiceTransitionException.class,
                () -> resolver.resolve(status, event)
        );
    }
}
