package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
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
public class CashPaymentDetailsRepositoryAdapterIntegrationTest
        extends AbstractPostgreSQLContainerTest {

    @Autowired
    private JpaCashPaymentDetailsRepository repository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private CashPaymentDetailsRepositoryAdapter adapter;

    private TransactionTemplate transactionTemplate;

    private static final Long INVOICE_ID = 12L;
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", EUR);
    private static final Money RECEIVED_AMOUNT = Money.of("450", EUR);
    private static final Money ANOTHER_RECEIVED_AMOUNT = Money.of("1000", EUR);
    private static final PaymentMethod METHOD = PaymentMethod.CASH;
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    @BeforeEach
    void setUp() {

        transactionTemplate = new TransactionTemplate(transactionManager);

        repository.deleteAll();
        jpaPaymentRepository.deleteAll();
    }

    @Test
    void save_should_persist_cash_details_after_commit() {

        Long paymentId = getSavedPaymentId();

        transactionTemplate.execute(status -> {

            adapter.save(getDetails(), paymentId);
            return null;
        });

        CashPaymentMethodDetails savedDetails = adapter.findByPaymentId(paymentId)
                .orElseThrow();

        assertNotNull(savedDetails);
        assertEquals(RECEIVED_AMOUNT, savedDetails.receivedAmount());
    }

    @Test
    void save_should_rollback_cash_details_when_transaction_fails() {

        Long paymentId = getSavedPaymentId();

        transactionTemplate.execute(status -> {

            adapter.save(getDetails(), paymentId);

            status.setRollbackOnly();
            return null;
        });

        assertTrue(adapter.findByPaymentId(paymentId).isEmpty());

        Payment paymentDB = paymentRepository.findById(paymentId).orElseThrow();

        assertNotNull(paymentDB);
        assertEquals(paymentId, paymentDB.getId());
    }

    @Test
    void save_should_reject_when_no_active_transaction() {

        Long paymentId = getSavedPaymentId();

        assertThrows(IllegalTransactionStateException.class,
                () -> adapter.save(getDetails(), paymentId));

        assertTrue(adapter.findByPaymentId(paymentId).isEmpty());

        Payment paymentDB = paymentRepository.findById(paymentId).orElseThrow();

        assertNotNull(paymentDB);
        assertEquals(paymentId, paymentDB.getId());
    }

    @Test
    void save_should_reject_duplicate_payment_id() {

        Long paymentId = getSavedPaymentId();

        transactionTemplate.execute(status -> {

            adapter.save(getDetails(), paymentId);
            return null;
        });

        assertThrows(DataIntegrityViolationException.class,
                () -> transactionTemplate.execute(status -> {

                    CashPaymentMethodDetails details =
                            new CashPaymentMethodDetails(ANOTHER_RECEIVED_AMOUNT);

                    adapter.save(details, paymentId);
                    return null;
                })
        );

        CashPaymentMethodDetails savedDetails = adapter.findByPaymentId(paymentId)
                .orElseThrow();

        assertNotNull(savedDetails);
        assertEquals(RECEIVED_AMOUNT, savedDetails.receivedAmount());
    }

    @Test
    void save_should_reject_details_for_nonexistent_payment() {

        Long paymentId = getSavedPaymentId();

        jpaPaymentRepository.deleteById(paymentId);
        assertFalse(jpaPaymentRepository.existsById(paymentId));

        assertThrows(DataIntegrityViolationException.class,
                () -> transactionTemplate.execute(status -> {

                    adapter.save(getDetails(), paymentId);
                    return null;
                })
        );

        assertTrue(adapter.findByPaymentId(paymentId).isEmpty());
    }

    @Test
    void findByPaymentId_should_return_empty_when_not_found() {

        Long paymentId = getSavedPaymentId();

        assertTrue(jpaPaymentRepository.existsById(paymentId));
        assertTrue(adapter.findByPaymentId(paymentId).isEmpty());
    }

    @Test
    void should_maintain_data_isolation_when_saving_multiple_payments() {

        Long paymentId1 = getSavedPaymentId();

        transactionTemplate.execute(status -> {

            adapter.save(getDetails(), paymentId1);
            return null;
        });

        Payment payment2 = new Payment(
                28L,
                Money.of("182.1", EUR),
                METHOD,
                FIXED_CLOCK.instant().plusSeconds(12));

        Payment saved2 = paymentRepository.save(payment2);

        assertNotNull(saved2);

        Long paymentId2 = saved2.getId();

        assertNotEquals(paymentId1, paymentId2);

        transactionTemplate.execute(status -> {

            adapter.save(new CashPaymentMethodDetails(ANOTHER_RECEIVED_AMOUNT)
                    , paymentId2);
            return null;
        });

        CashPaymentMethodDetails savedDetails1 = adapter.findByPaymentId(paymentId1)
                .orElseThrow();

        assertNotNull(savedDetails1);
        assertEquals(RECEIVED_AMOUNT, savedDetails1.receivedAmount());

        CashPaymentMethodDetails savedDetails2 = adapter.findByPaymentId(paymentId2)
                .orElseThrow();

        assertNotNull(savedDetails2);
        assertEquals(ANOTHER_RECEIVED_AMOUNT, savedDetails2.receivedAmount());
    }

    private Long getSavedPaymentId() {
        Payment payment = getCreatedPayment();

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved);

        Long paymentId = saved.getId();

        assertNotNull(paymentId);
        assertEquals(PaymentStatus.CREATED, saved.getStatus());
        assertNull(saved.getUpdatedAt());
        assertNull(saved.getCompletedAt());

        return paymentId;
    }

    private CashPaymentMethodDetails getDetails() {
        return new CashPaymentMethodDetails(RECEIVED_AMOUNT);
    }

    private Payment getCreatedPayment() {
        return new Payment(
                INVOICE_ID,
                AMOUNT,
                METHOD,
                FIXED_CLOCK.instant());
    }
}
