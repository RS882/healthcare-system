package com.healthcare.billing.model.entity.invoice;

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

        InvoiceStatus result = resolver.resolve(
                InvoiceStatus.DRAFT,
                InvoiceEvent.ISSUE
        );

        assertEquals(InvoiceStatus.ISSUED, result);
    }

    @Test
    void shouldResolveDraftToCancelled() {

        InvoiceStatus result = resolver.resolve(
                InvoiceStatus.DRAFT,
                InvoiceEvent.CANCEL
        );

        assertEquals(InvoiceStatus.CANCELLED, result);
    }

    @Test
    void shouldResolveIssuedToPaid() {

        InvoiceStatus result = resolver.resolve(
                InvoiceStatus.ISSUED,
                InvoiceEvent.PAY
        );

        assertEquals(InvoiceStatus.PAID, result);
    }

    @Test
    void shouldResolveIssuedToCancelled() {

        InvoiceStatus result = resolver.resolve(
                InvoiceStatus.ISSUED,
                InvoiceEvent.CANCEL
        );

        assertEquals(InvoiceStatus.CANCELLED, result);
    }

    @Test
    void shouldRejectPaymentFromDraft() {

        assertThrows(IllegalStateException.class,
                () -> resolver.resolve(InvoiceStatus.DRAFT, InvoiceEvent.PAY)
        );
    }

    @Test
    void shouldRejectIssueFromIssued() {

        assertThrows(IllegalStateException.class,
                () -> resolver.resolve(InvoiceStatus.ISSUED, InvoiceEvent.ISSUE)
        );
    }

    @Test
    void shouldRejectIssueFromPaid() {

        assertThrows(IllegalStateException.class,
                () -> resolver.resolve(InvoiceStatus.PAID, InvoiceEvent.ISSUE)
        );
    }

    @Test
    void shouldRejectPaymentFromPaid() {

        assertThrows(IllegalStateException.class,
                () -> resolver.resolve(InvoiceStatus.PAID, InvoiceEvent.PAY)
        );
    }

    @Test
    void shouldRejectCancellationFromPaid() {

        assertThrows(IllegalStateException.class,
                () -> resolver.resolve(InvoiceStatus.PAID, InvoiceEvent.CANCEL)
        );
    }

    @Test
    void shouldRejectTransitionFromCancelled() {

        assertThrows(IllegalStateException.class,
                () -> resolver.resolve(InvoiceStatus.CANCELLED, InvoiceEvent.ISSUE)
        );
    }
}