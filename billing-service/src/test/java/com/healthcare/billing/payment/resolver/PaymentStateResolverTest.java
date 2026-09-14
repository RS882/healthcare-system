package com.healthcare.billing.payment.resolver;

import com.healthcare.billing.exception.PaymentValidationException;
import com.healthcare.billing.payment.model.*;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Payment state resolver tests: ")
class PaymentStateResolverTest {

    private PaymentStateResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new PaymentStateResolver();
    }

    @Test
    void should_return_same_state_instance_for_same_status() {
        PaymentState first = resolver.resolve(PaymentStatus.CREATED);
        PaymentState second = resolver.resolve(PaymentStatus.CREATED);

        assertThat(first).isSameAs(second);
    }

    @Test
    void should_resolve_created_state() {
        assertThat(resolver.resolve(PaymentStatus.CREATED))
                .isInstanceOf(CreatedPaymentState.class);
    }

    @Test
    void should_resolve_pending_state() {
        assertThat(resolver.resolve(PaymentStatus.PENDING))
                .isInstanceOf(PendingPaymentState.class);
    }

    @Test
    void should_resolve_completed_state() {
        assertThat(resolver.resolve(PaymentStatus.COMPLETED))
                .isInstanceOf(CompletedPaymentState.class);
    }

    @Test
    void should_resolve_cancelled_state() {
        assertThat(resolver.resolve(PaymentStatus.CANCELLED))
                .isInstanceOf(CancelledPaymentState.class);
    }

    @Test
    void should_throw_exception_when_status_is_null() {
        assertThatThrownBy(() -> resolver.resolve(null))
                .isInstanceOf(PaymentValidationException.class);
    }
}