package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.exception.PaymentJpaAdapterException;
import com.healthcare.billing.exception.PaymentNotFoundException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.persistence.entity.PaymentEntity;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "stub"})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PaymentUpdateTransactionIntegrationTest
        extends AbstractPostgreSQLContainerTest {

    private TransactionTemplate transactionTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final Long ID = 73L;
    private static final Long INVOICE_ID = 12L;
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", CURRENCY);
    private static final PaymentMethod METHOD = PaymentMethod.CARD;
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    @BeforeEach
    void setUp() {

        transactionTemplate = new TransactionTemplate(transactionManager);
        jpaPaymentRepository.deleteAll();
    }

    @Test
    void update_should_require_existing_transaction() {

        Payment payment = getCreatedPayment();

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(PaymentStatus.CREATED, saved.getStatus());
        assertNull(saved.getUpdatedAt());
        assertNull(saved.getCompletedAt());

        assertThrows(IllegalTransactionStateException.class,
                () -> paymentRepository.update(saved));
    }

    @Test
    void update_should_persist_changes_after_commit() {

        Instant startUpdatedAt = FIXED_CLOCK.instant().plusSeconds(2);
        Instant completedAt = FIXED_CLOCK.instant().plusSeconds(3);

        Payment payment = getCreatedPayment();

        PaymentStateResolver resolver = new PaymentStateResolver();

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(PaymentStatus.CREATED, saved.getStatus());
        assertNull(saved.getUpdatedAt());
        assertNull(saved.getCompletedAt());

        transactionTemplate.execute(status -> {

                    saved.start(startUpdatedAt, resolver);
                    saved.complete(completedAt, resolver);

                    paymentRepository.update(saved);

                    return null;
                }
        );

        Payment updated = paymentRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals(PaymentStatus.COMPLETED, updated.getStatus());
        assertEquals(completedAt, updated.getUpdatedAt());
        assertEquals(completedAt, updated.getCompletedAt());

        assertEquals(saved.getId(), updated.getId());
        assertEquals(INVOICE_ID, updated.getInvoiceId());
        assertEquals(AMOUNT, updated.getAmount());
        assertEquals(METHOD, updated.getMethod());
        assertEquals(FIXED_CLOCK.instant(), updated.getCreatedAt());
    }

    @Test
    void update_should_not_persist_changes_after_rollback() {

        Instant startUpdatedAt = FIXED_CLOCK.instant().plusSeconds(2);
        Instant completedAt = FIXED_CLOCK.instant().plusSeconds(3);

        Payment payment = getCreatedPayment();

        PaymentStateResolver resolver = new PaymentStateResolver();

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved);
        assertNotNull(saved.getId());

        assertEquals(PaymentStatus.CREATED, saved.getStatus());
        assertNull(saved.getUpdatedAt());
        assertNull(saved.getCompletedAt());

        transactionTemplate.execute(status -> {

                    saved.start(startUpdatedAt, resolver);
                    saved.complete(completedAt, resolver);

                    paymentRepository.update(saved);

                    assertEquals(PaymentStatus.COMPLETED, saved.getStatus());
                    assertEquals(completedAt, saved.getUpdatedAt());
                    assertEquals(completedAt, saved.getCompletedAt());

                    status.setRollbackOnly();

                    return null;
                }
        );

        Payment updated = paymentRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals(PaymentStatus.CREATED, updated.getStatus());
        assertNull(updated.getUpdatedAt());
        assertNull(updated.getCompletedAt());

        assertEquals(saved.getId(), updated.getId());
        assertEquals(INVOICE_ID, updated.getInvoiceId());
        assertEquals(AMOUNT, updated.getAmount());
        assertEquals(METHOD, updated.getMethod());
        assertEquals(FIXED_CLOCK.instant(), updated.getCreatedAt());
    }

    @Test
    void update_should_not_persist_changes_after_exception() {

        Instant startUpdatedAt = FIXED_CLOCK.instant().plusSeconds(2);
        Instant completedAt = FIXED_CLOCK.instant().plusSeconds(3);
        String exceptionMessage = "Simulated failure after payment update";

        Payment payment = getCreatedPayment();

        PaymentStateResolver resolver = new PaymentStateResolver();

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved);
        assertNotNull(saved.getId());

        assertEquals(PaymentStatus.CREATED, saved.getStatus());
        assertNull(saved.getUpdatedAt());
        assertNull(saved.getCompletedAt());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transactionTemplate.execute(status -> {

                            saved.start(startUpdatedAt, resolver);
                            saved.complete(completedAt, resolver);

                            paymentRepository.update(saved);

                            assertEquals(PaymentStatus.COMPLETED, saved.getStatus());
                            assertEquals(completedAt, saved.getUpdatedAt());
                            assertEquals(completedAt, saved.getCompletedAt());

                            throw new IllegalStateException(exceptionMessage);
                        }
                )
        );

        assertEquals(exceptionMessage, exception.getMessage());

        Payment updated = paymentRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals(PaymentStatus.CREATED, updated.getStatus());
        assertNull(updated.getUpdatedAt());
        assertNull(updated.getCompletedAt());

        assertEquals(saved.getId(), updated.getId());
        assertEquals(INVOICE_ID, updated.getInvoiceId());
        assertEquals(AMOUNT, updated.getAmount());
        assertEquals(METHOD, updated.getMethod());
        assertEquals(FIXED_CLOCK.instant(), updated.getCreatedAt());
    }

    @Test
    void update_should_throw_when_payment_not_found() {

        Instant startUpdatedAt = FIXED_CLOCK.instant().plusSeconds(2);
        Instant completedAt = FIXED_CLOCK.instant().plusSeconds(3);

        Payment payment = getCreatedPayment();

        PaymentStateResolver resolver = new PaymentStateResolver();

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved);
        assertNotNull(saved.getId());

        assertEquals(PaymentStatus.CREATED, saved.getStatus());
        assertNull(saved.getUpdatedAt());
        assertNull(saved.getCompletedAt());

        Long paymentId = saved.getId();

        jpaPaymentRepository.deleteById(paymentId);

        assertFalse(jpaPaymentRepository.existsById(paymentId));

        assertThrows(PaymentNotFoundException.class,
                () -> transactionTemplate.execute(status -> {

                            saved.start(startUpdatedAt, resolver);
                            saved.complete(completedAt, resolver);

                            paymentRepository.update(saved);

                            return null;
                        }
                )
        );
        assertFalse(jpaPaymentRepository.existsById(paymentId));
    }

    @Test
    void update_should_reject_immutable_field_mismatch() {
        Instant startUpdatedAt = FIXED_CLOCK.instant().plusSeconds(2);
        Instant completedAt = FIXED_CLOCK.instant().plusSeconds(3);

        Payment payment = getCreatedPayment();

        PaymentStateResolver resolver = new PaymentStateResolver();

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved);
        Long paymentId = saved.getId();
        assertNotNull(paymentId);

        Payment incorrectPayment = Payment.reconstitute(
                paymentId,
                999L,
                AMOUNT,
                METHOD,
                PaymentStatus.CREATED,
                FIXED_CLOCK.instant(),
                null,
                null
        );

        assertThrows(PaymentJpaAdapterException.class,
                () -> transactionTemplate.execute(status -> {

                            incorrectPayment.start(startUpdatedAt, resolver);
                            incorrectPayment.complete(completedAt, resolver);

                            paymentRepository.update(incorrectPayment);

                            return null;
                        }
                )
        );

        Payment updated = paymentRepository.findById(saved.getId())
                .orElseThrow();

        assertEquals(PaymentStatus.CREATED, updated.getStatus());
        assertNull(updated.getUpdatedAt());
        assertNull(updated.getCompletedAt());

        assertEquals(saved.getId(), updated.getId());
        assertEquals(INVOICE_ID, updated.getInvoiceId());
        assertEquals(AMOUNT, updated.getAmount());
        assertEquals(METHOD, updated.getMethod());
        assertEquals(FIXED_CLOCK.instant(), updated.getCreatedAt());


    }

    private Payment getCreatedPayment() {
        return new Payment(
                INVOICE_ID,
                AMOUNT,
                METHOD,
                FIXED_CLOCK.instant());
    }
}