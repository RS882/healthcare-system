package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetailsPort;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import com.healthcare.billing.payment.payment_method_details.persistence_strategy.CardPaymentDetailsPersistenceStrategy;
import com.healthcare.billing.payment.payment_method_details.persistence_strategy.CashPaymentDetailsPersistenceStrategy;
import com.healthcare.billing.payment.payment_method_details.persistence_strategy.PaymentDetailsAdapterRegistry;
import com.healthcare.billing.payment.payment_method_details.persistence_strategy.PaymentDetailsPersistenceStrategy;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "stub"})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PaymentMethodDetailsPersistenceAdapterIntegrationTest
        extends AbstractPostgreSQLContainerTest {

    @Autowired
    private JpaCardPaymentDetailsRepository jpaCardPaymentDetailsRepository;

    @Autowired
    private JpaCashPaymentDetailsRepository jpaCashPaymentDetailsRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private PaymentMethodDetailsPort paymentMethodDetailsPort;

    @Autowired
    private PaymentDetailsAdapterRegistry paymentDetailsAdapterRegistry;

    private TransactionTemplate transactionTemplate;

    private static final Long INVOICE_ID = 12L;
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Money AMOUNT = Money.of("328.29", EUR);
    private static final Money RECEIVED_AMOUNT = Money.of("450", EUR);
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    private static final CardPaymentProvider PROVIDER = CardPaymentProvider.ADYEN;
    private static final String PROVIDER_TRANSACTION_ID = "Test transaction ID";

    @BeforeEach
    void setUp() {

        transactionTemplate = new TransactionTemplate(transactionManager);

        jpaCardPaymentDetailsRepository.deleteAll();
        jpaCashPaymentDetailsRepository.deleteAll();
        jpaPaymentRepository.deleteAll();
    }

    @Test
    void should_save_and_retrieve_card_payment_details_after_commit() {

        PaymentMethod method = PaymentMethod.CARD;
        Long paymentId = getSavedPaymentId(method);
        CardPaymentMethodDetails details = getCardDetails();

        transactionTemplate.execute(status -> {
            paymentMethodDetailsPort.savePaymentMethodDetails(paymentId, details);
            return null;
        });

        Optional<PaymentMethodDetails> savedDetails =
                paymentMethodDetailsPort.getPaymentMethodDetails(paymentId, method);

        assertTrue(savedDetails.isPresent());
        assertInstanceOf(CardPaymentMethodDetails.class, savedDetails.orElseThrow());

        CardPaymentMethodDetails cardDetails = (CardPaymentMethodDetails) savedDetails.orElseThrow();
        assertEquals(details, cardDetails);
    }

    @Test
    void should_save_and_retrieve_cash_payment_details_after_commit() {

        PaymentMethod method = PaymentMethod.CASH;
        Long paymentId = getSavedPaymentId(method);
        CashPaymentMethodDetails details = getCashDetails();

        transactionTemplate.execute(status -> {
            paymentMethodDetailsPort.savePaymentMethodDetails(paymentId, details);
            return null;
        });

        Optional<PaymentMethodDetails> savedDetails =
                paymentMethodDetailsPort.getPaymentMethodDetails(paymentId, method);

        assertTrue(savedDetails.isPresent());
        assertInstanceOf(CashPaymentMethodDetails.class, savedDetails.orElseThrow());

        CashPaymentMethodDetails cashDetails = (CashPaymentMethodDetails) savedDetails.orElseThrow();
        assertEquals(details, cashDetails);
    }

    @ParameterizedTest(name = "Test {index}: payment method [{arguments}]")
    @EnumSource(value = PaymentMethod.class,
            names = {"CARD", "CASH"})
    void should_return_optional_empty_when_details_not_found(PaymentMethod method) {

        Long paymentId = getSavedPaymentId(method);

        Optional<PaymentMethodDetails> savedDetails =
                paymentMethodDetailsPort.getPaymentMethodDetails(paymentId, method);

        assertFalse(savedDetails.isPresent());
        assertTrue(jpaPaymentRepository.existsById(paymentId));
    }

    @ParameterizedTest(name = "Test {index}: method, payment method details[{arguments}]")
    @MethodSource("paymentDetails")
    void should_do_not_save_payment_details_when_rollback(
            PaymentMethod method,
            PaymentMethodDetails details
    ) {

        Long paymentId = getSavedPaymentId(method);

        transactionTemplate.execute(status -> {
            paymentMethodDetailsPort.savePaymentMethodDetails(paymentId, details);

            status.setRollbackOnly();
            return null;
        });

        Optional<PaymentMethodDetails> savedDetails =
                paymentMethodDetailsPort.getPaymentMethodDetails(paymentId, method);

        assertFalse(savedDetails.isPresent());
        assertTrue(jpaPaymentRepository.existsById(paymentId));
    }

    @Test
    void should_keep_card_and_cash_payment_details_separate() {

        PaymentMethod methodCard = PaymentMethod.CARD;
        Long cardPaymentId = getSavedPaymentId(methodCard);
        CardPaymentMethodDetails cardDetails = getCardDetails();

        transactionTemplate.execute(status -> {
            paymentMethodDetailsPort.savePaymentMethodDetails(cardPaymentId, cardDetails);
            return null;
        });

        PaymentMethod methodCash = PaymentMethod.CASH;
        Long cashPaymentId = getSavedPaymentId(methodCash);
        CashPaymentMethodDetails cashDetails = getCashDetails();

        transactionTemplate.execute(status -> {
            paymentMethodDetailsPort.savePaymentMethodDetails(cashPaymentId, cashDetails);
            return null;
        });

        Optional<PaymentMethodDetails> savedCardDetails =
                paymentMethodDetailsPort.getPaymentMethodDetails(cardPaymentId, methodCard);

        Optional<PaymentMethodDetails> savedCashDetails =
                paymentMethodDetailsPort.getPaymentMethodDetails(cashPaymentId, methodCash);

        assertTrue(savedCardDetails.isPresent());
        assertTrue(savedCashDetails.isPresent());

        assertInstanceOf(CardPaymentMethodDetails.class, savedCardDetails.orElseThrow());
        assertInstanceOf(CashPaymentMethodDetails.class, savedCashDetails.orElseThrow());

        CardPaymentMethodDetails cardSavedDetails = (CardPaymentMethodDetails) savedCardDetails.orElseThrow();
        assertEquals(cardDetails, cardSavedDetails);

        CashPaymentMethodDetails cashSavedDetails = (CashPaymentMethodDetails) savedCashDetails.orElseThrow();
        assertEquals(cashDetails, cashSavedDetails);

        assertTrue(paymentMethodDetailsPort
                .getPaymentMethodDetails(cardPaymentId, PaymentMethod.CASH)
                .isEmpty());

        assertTrue(paymentMethodDetailsPort
                .getPaymentMethodDetails(cashPaymentId, PaymentMethod.CARD)
                .isEmpty());
    }

    @ParameterizedTest(name = "Test {index}: strategy class, method[{arguments}]")
    @MethodSource("strategy")
    void should_return_correct_payment_details_strategy(
            Class<PaymentDetailsPersistenceStrategy> strategyClass,
            PaymentMethod method
    ) {
        PaymentDetailsPersistenceStrategy strategy =
                paymentDetailsAdapterRegistry.getStrategy(method);

        assertInstanceOf(strategyClass, strategy);
    }

    private static Stream<Arguments> strategy() {
        return Stream.of(
                Arguments.of(
                        CardPaymentDetailsPersistenceStrategy.class,
                        PaymentMethod.CARD
                ),
                Arguments.of(
                        CashPaymentDetailsPersistenceStrategy.class,
                        PaymentMethod.CASH
                )
        );
    }

    private static Stream<Arguments> paymentDetails() {
        return Stream.of(
                Arguments.of(
                        PaymentMethod.CARD,
                        getCardDetails()
                ),
                Arguments.of(
                        PaymentMethod.CASH,
                        getCashDetails()
                )
        );
    }

    private Long getSavedPaymentId(PaymentMethod method) {
        Payment payment = getCreatedPayment(method);

        Payment saved = paymentRepository.save(payment);

        assertNotNull(saved);

        Long paymentId = saved.getId();

        assertNotNull(paymentId);
        assertEquals(PaymentStatus.CREATED, saved.getStatus());
        assertNull(saved.getUpdatedAt());
        assertNull(saved.getCompletedAt());

        return paymentId;
    }

    @Test
    void should_return_exception_when_details_save_one_transaction() {

        PaymentMethod method = PaymentMethod.CARD;
        Long paymentId = getSavedPaymentId(method);
        CardPaymentMethodDetails details = getCardDetails();

        assertThrows(IllegalTransactionStateException.class,
                () -> paymentMethodDetailsPort.savePaymentMethodDetails(paymentId, details));
    }

    private Payment getCreatedPayment(PaymentMethod method) {
        return new Payment(
                INVOICE_ID,
                AMOUNT,
                method,
                FIXED_CLOCK.instant());
    }

    private static CardPaymentMethodDetails getCardDetails() {
        return new CardPaymentMethodDetails(PROVIDER_TRANSACTION_ID, PROVIDER);
    }

    private static CashPaymentMethodDetails getCashDetails() {
        return new CashPaymentMethodDetails(RECEIVED_AMOUNT);
    }
}