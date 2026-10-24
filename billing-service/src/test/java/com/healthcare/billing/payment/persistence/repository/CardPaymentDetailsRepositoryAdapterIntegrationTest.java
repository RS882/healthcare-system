package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
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
public class CardPaymentDetailsRepositoryAdapterIntegrationTest
        extends AbstractPostgreSQLContainerTest {

    @Autowired
    private JpaCardPaymentDetailsRepository repository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private CardPaymentDetailsRepositoryAdapter adapter;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {

        transactionTemplate = new TransactionTemplate(transactionManager);

        repository.deleteAll();
        jpaPaymentRepository.deleteAll();
    }

    private static final Long INVOICE_ID = 12L;
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", CURRENCY);
    private static final PaymentMethod METHOD = PaymentMethod.CARD;
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    private static final CardPaymentProvider PROVIDER = CardPaymentProvider.ADYEN;
    private static final String PROVIDER_TRANSACTION_ID = "Test transaction ID";

    @Test
    void save_should_persist_card_details_after_commit() {

        Long paymentId = getSavedPaymentId();

        transactionTemplate.execute(status -> {

            adapter.save(getDetails(), paymentId);
            return null;
        });

        CardPaymentMethodDetails savedDetails = adapter.findByPaymentId(paymentId).orElseThrow();

        assertNotNull(savedDetails);
        assertEquals(PROVIDER_TRANSACTION_ID, savedDetails.providerTransactionId());
        assertEquals(PROVIDER, savedDetails.provider());
    }

    @Test
    void save_should_rollback_card_details_when_transaction_fails() {

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

                    CardPaymentMethodDetails details =
                            new CardPaymentMethodDetails(
                                    "Another test transaction ID",
                                    PROVIDER
                            );

                    adapter.save(details, paymentId);
                    return null;
                })
        );

        CardPaymentMethodDetails savedDetails = adapter.findByPaymentId(paymentId).orElseThrow();

        assertNotNull(savedDetails);
        assertEquals(PROVIDER_TRANSACTION_ID, savedDetails.providerTransactionId());
        assertEquals(PROVIDER, savedDetails.provider());
    }

    @Test
    void save_should_reject_duplicate_provider_transaction(){

        Long paymentId1 = getSavedPaymentId();

        CardPaymentMethodDetails details =getDetails();

        transactionTemplate.execute(status -> {

            adapter.save(details, paymentId1);
            return null;
        });

        Payment payment2 = new Payment(
                28L,
                Money.of("182.1", CURRENCY),
                METHOD,
                FIXED_CLOCK.instant().plusSeconds(12));

        Payment saved2 = paymentRepository.save(payment2);

        assertNotNull(saved2);

        Long paymentId2 = saved2.getId();

        assertNotEquals(paymentId1, paymentId2);

        assertThrows(DataIntegrityViolationException.class,
                ()->    transactionTemplate.execute(status -> {

                    adapter.save(details, paymentId2);
                    return null;
                })
        );

        CardPaymentMethodDetails savedDetails = adapter.findByPaymentId(paymentId1).orElseThrow();

        assertNotNull(savedDetails);
        assertEquals(PROVIDER_TRANSACTION_ID, savedDetails.providerTransactionId());
        assertEquals(PROVIDER, savedDetails.provider());

        assertTrue(adapter.findByPaymentId(paymentId2).isEmpty());
    }

    @Test
    void save_should_allow_same_transaction_id_for_different_providers(){

        Long paymentId1 = getSavedPaymentId();

        CardPaymentMethodDetails details1 =getDetails();

        transactionTemplate.execute(status -> {

            adapter.save(details1, paymentId1);
            return null;
        });

        Payment payment2 = new Payment(
                28L,
                Money.of("182.1", CURRENCY),
                METHOD,
                FIXED_CLOCK.instant().plusSeconds(12));

        Payment saved2 = paymentRepository.save(payment2);

        assertNotNull(saved2);

        Long paymentId2 = saved2.getId();

        assertNotEquals(paymentId1, paymentId2);

        CardPaymentProvider provider2 = CardPaymentProvider.STRIPE;
        CardPaymentMethodDetails details2 = new CardPaymentMethodDetails(
                PROVIDER_TRANSACTION_ID,
                provider2);

        transactionTemplate.execute(status -> {

            adapter.save(details2, paymentId2);
            return null;
        });

        CardPaymentMethodDetails savedDetails1 = adapter.findByPaymentId(paymentId1).orElseThrow();

        assertNotNull(savedDetails1);
        assertEquals(PROVIDER_TRANSACTION_ID, savedDetails1.providerTransactionId());
        assertEquals(PROVIDER, savedDetails1.provider());

        CardPaymentMethodDetails savedDetails2 = adapter.findByPaymentId(paymentId2).orElseThrow();

        assertNotNull(savedDetails2);
        assertEquals(PROVIDER_TRANSACTION_ID, savedDetails2.providerTransactionId());
        assertEquals(provider2, savedDetails2.provider());
    }

    @Test
    void save_should_reject_details_for_nonexistent_payment(){

        Long paymentId = getSavedPaymentId();

        jpaPaymentRepository.deleteById(paymentId);
        assertFalse(jpaPaymentRepository.existsById(paymentId));

        assertThrows(DataIntegrityViolationException.class,
                ()->    transactionTemplate.execute(status -> {

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

    private CardPaymentMethodDetails getDetails() {
        return new CardPaymentMethodDetails(PROVIDER_TRANSACTION_ID, PROVIDER);
    }

    private Payment getCreatedPayment() {
        return new Payment(
                INVOICE_ID,
                AMOUNT,
                METHOD,
                FIXED_CLOCK.instant());
    }
}
