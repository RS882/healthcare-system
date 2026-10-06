package com.healthcare.billing.payment.persistence.repository;

import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.model.enums.PaymentMethod;
import com.healthcare.billing.payment.model.enums.PaymentStatus;
import com.healthcare.billing.payment.persistence.dto.AmountDto;
import com.healthcare.billing.payment.persistence.entity.PaymentEntity;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.payment.resolver.PaymentStateResolver;
import com.healthcare.billing.test_container.AbstractPostgreSQLContainerTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PaymentPersistenceIntegrationTest extends AbstractPostgreSQLContainerTest {

    @Autowired
    private JpaPaymentRepository jpaPaymentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;

    private static final Long INVOICE_ID = 12L;
    private static final Currency CURRENCY_EUR = Currency.getInstance("EUR");
    private static final Currency CURRENCY_USD = Currency.getInstance("USD");
    private static final Money AMOUNT = Money.of("328.29", CURRENCY_EUR);
    private static final PaymentMethod METHOD = PaymentMethod.CARD;
    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-09-04T10:00:00Z"), ZONE);

    @BeforeEach
    void setUp() {

        jpaPaymentRepository.deleteAll();
        jpaPaymentRepository.flush();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0.00",
            "-10.00"
    })
    void should_reject_payment_entity_when_amount_is_not_positive(String amount) {

        PaymentEntity entity = getEntityWithStatusCreated();
        entity.setAmount(new BigDecimal(amount));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jpaPaymentRepository.saveAndFlush(entity)
        );
    }

    @Test
    void should_save_created_payment() {

        PaymentStatus status = PaymentStatus.CREATED;

        Payment payment = getCreatedPayment();

        Payment savedPayment = paymentRepository.save(payment);

        assertNotNull(savedPayment);

        Long paymentId = savedPayment.getId();

        assertTrue(paymentId > 0);
        assertEquals(INVOICE_ID, savedPayment.getInvoiceId());
        assertEquals(AMOUNT, savedPayment.getAmount());
        assertEquals(METHOD, savedPayment.getMethod());
        assertEquals(status, savedPayment.getStatus());
        assertEquals(FIXED_CLOCK.instant(), savedPayment.getCreatedAt());
        assertNull(savedPayment.getUpdatedAt());
        assertNull(savedPayment.getCompletedAt());

        clearPersistenceContext();

        Optional<Payment> optionalFoundPayment = paymentRepository.findById(paymentId);

        assertTrue(optionalFoundPayment.isPresent());

        Payment foundPayment = optionalFoundPayment.get();

        assertEquals(paymentId, foundPayment.getId());
        assertEquals(INVOICE_ID, foundPayment.getInvoiceId());
        assertEquals(AMOUNT, foundPayment.getAmount());
        assertEquals(METHOD, foundPayment.getMethod());
        assertEquals(status, foundPayment.getStatus());
        assertEquals(FIXED_CLOCK.instant(), foundPayment.getCreatedAt());
        assertNull(foundPayment.getUpdatedAt());
        assertNull(foundPayment.getCompletedAt());
    }

    @Test
    void should_return_empty_when_payment_not_found_by_id() {

        Optional<Payment> result = paymentRepository.findById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void should_find_all_payments_by_invoice_id() {

        Payment payment1 = getCreatedPayment();
        Payment payment2 = getCreatedPayment("298.00");
        Payment payment3 = getCreatedPayment(9999L);

        Payment savedPayment1 = paymentRepository.save(payment1);
        Payment savedPayment2 = paymentRepository.save(payment2);
        Payment savedPayment3 = paymentRepository.save(payment3);

        clearPersistenceContext();

        List<Payment> result =
                paymentRepository.findByInvoiceId(INVOICE_ID);

        assertNotNull(result);
        assertEquals(2, result.size());

        List<Long> resultIds = result.stream()
                .map(Payment::getId)
                .toList();

        assertTrue(resultIds.contains(savedPayment1.getId()));
        assertTrue(resultIds.contains(savedPayment2.getId()));
        assertFalse(resultIds.contains(savedPayment3.getId()));
    }

    @Test
    void should_return_empty_list_when_invoice_has_no_payments() {

        List<Payment> result =
                paymentRepository.findByInvoiceId(INVOICE_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void should_persist_completed_payment_lifecycle() {

        PaymentStateResolver resolver = new PaymentStateResolver();
        Instant updatedAt = FIXED_CLOCK.instant().plusSeconds(3);
        Instant completedAt = FIXED_CLOCK.instant().plusSeconds(10);

        Payment payment = getCreatedPayment();

        Payment savedCreatedPayment = paymentRepository.save(payment);
        Long paymentId = savedCreatedPayment.getId();
        clearPersistenceContext();

        Payment foundCreatedPayment = paymentRepository.findById(paymentId).orElseThrow();
        foundCreatedPayment.start(updatedAt, resolver);
        paymentRepository.save(foundCreatedPayment);
        clearPersistenceContext();

        Payment foundPendingPayment = paymentRepository.findById(paymentId).orElseThrow();
        foundPendingPayment.complete(completedAt, resolver);
        paymentRepository.save(foundPendingPayment);
        clearPersistenceContext();

        Payment foundCompletedPayment = paymentRepository.findById(paymentId).orElseThrow();
        List<Payment> payments = paymentRepository.findByInvoiceId(INVOICE_ID);

        assertEquals(1, payments.size());
        assertEquals(paymentId, foundCompletedPayment.getId());
        assertEquals(INVOICE_ID, foundCompletedPayment.getInvoiceId());
        assertEquals(AMOUNT, foundCompletedPayment.getAmount());
        assertEquals(METHOD, foundCompletedPayment.getMethod());
        assertEquals(PaymentStatus.COMPLETED, foundCompletedPayment.getStatus());
        assertEquals(FIXED_CLOCK.instant(), foundCompletedPayment.getCreatedAt());
        assertEquals(completedAt, foundCompletedPayment.getUpdatedAt());
        assertEquals(completedAt, foundCompletedPayment.getCompletedAt());
    }

    @Test
    void should_persist_cancelled_payment_lifecycle() {

        PaymentStateResolver resolver = new PaymentStateResolver();
        Instant updatedAt = FIXED_CLOCK.instant().plusSeconds(3);

        Payment payment = getCreatedPayment();

        Payment savedCreatedPayment = paymentRepository.save(payment);
        Long paymentId = savedCreatedPayment.getId();
        clearPersistenceContext();

        Payment foundCreatedPayment = paymentRepository.findById(paymentId).orElseThrow();
        foundCreatedPayment.cancel(updatedAt, resolver);
        paymentRepository.save(foundCreatedPayment);
        clearPersistenceContext();

        Payment foundCanceledPayment = paymentRepository.findById(paymentId).orElseThrow();
        List<Payment> payments = paymentRepository.findByInvoiceId(INVOICE_ID);

        assertEquals(1, payments.size());
        assertEquals(paymentId, foundCanceledPayment.getId());
        assertEquals(INVOICE_ID, foundCanceledPayment.getInvoiceId());
        assertEquals(AMOUNT, foundCanceledPayment.getAmount());
        assertEquals(METHOD, foundCanceledPayment.getMethod());
        assertEquals(PaymentStatus.CANCELLED, foundCanceledPayment.getStatus());
        assertEquals(FIXED_CLOCK.instant(), foundCanceledPayment.getCreatedAt());
        assertEquals(updatedAt, foundCanceledPayment.getUpdatedAt());
        assertNull(foundCanceledPayment.getCompletedAt());
    }

    @Test
    void should_monetary_precision_across_payment_persistence_boundary() {

        String amount = "584.87000000";

        Payment payment = getCreatedPayment(amount);

        Payment savedPayment = paymentRepository.save(payment);

        Long paymentId = savedPayment.getId();

        clearPersistenceContext();

        Payment foundCanceledPayment = paymentRepository.findById(paymentId).orElseThrow();

        assertEquals(paymentId, foundCanceledPayment.getId());

        Money expectedAmount = Money.of(amount, CURRENCY_EUR);

        assertEquals(0, expectedAmount.compareTo(foundCanceledPayment.getAmount()));
    }

    @Test
    void should_return_paid_amount_when_completed_payments_exist() {

        PaymentStateResolver resolver = new PaymentStateResolver();

        String amount1 = "25.75";
        String amount2 = "100.1";
        String amount3 = "46.32000";

        Payment payment1 = getCreatedPayment(INVOICE_ID, amount1);
        Payment payment2 = getCreatedPayment(INVOICE_ID, amount2);
        Payment payment3 = getCreatedPayment(INVOICE_ID, amount3);

        Payment savedPayment1 = paymentRepository.save(payment1);
        Payment savedPayment2 = paymentRepository.save(payment2);
        Payment savedPayment3 = paymentRepository.save(payment3);
        clearPersistenceContext();

        savedPayment1.start(FIXED_CLOCK.instant().plusSeconds(3), resolver);
        savedPayment2.start(FIXED_CLOCK.instant().plusSeconds(2), resolver);
        savedPayment3.start(FIXED_CLOCK.instant().plusSeconds(1), resolver);
        savedPayment1.complete(FIXED_CLOCK.instant().plusSeconds(6), resolver);
        savedPayment2.complete(FIXED_CLOCK.instant().plusSeconds(5), resolver);
        savedPayment3.complete(FIXED_CLOCK.instant().plusSeconds(4), resolver);
        paymentRepository.save(savedPayment1);
        paymentRepository.save(savedPayment2);
        paymentRepository.save(savedPayment3);
        clearPersistenceContext();

        Money calculatedAmount = paymentRepository.calculatePaidAmount(INVOICE_ID);

        BigDecimal totalAmount = new BigDecimal(amount1)
                .add(new BigDecimal(amount2))
                .add(new BigDecimal(amount3)
                );

        assertNotNull(calculatedAmount);
        assertEquals(0, Money.of(totalAmount, CURRENCY_EUR).compareTo(calculatedAmount));
    }

    @Test
    void should_include_only_completed_payments_in_paid_amount() {

        PaymentStateResolver resolver = new PaymentStateResolver();

        String amount1 = "25.75";
        String amount2 = "100.1";
        String amount3 = "46.32000";
        String amount4 = "584.87000000";
        String amount5 = "51.75";

        Payment payment1 = getCreatedPayment(INVOICE_ID, amount1);
        Payment payment2 = getCreatedPayment(INVOICE_ID, amount2);
        Payment payment3 = getCreatedPayment(INVOICE_ID, amount3);
        Payment payment4 = getCreatedPayment(INVOICE_ID, amount4);
        Payment payment5 = getCreatedPayment(INVOICE_ID, amount5);

        Payment savedPayment1 = paymentRepository.save(payment1);
        Payment savedPayment2 = paymentRepository.save(payment2);
        Payment savedPayment3 = paymentRepository.save(payment3);
        Payment savedPayment4 = paymentRepository.save(payment4);
        paymentRepository.save(payment5);
        clearPersistenceContext();

        savedPayment1.start(FIXED_CLOCK.instant().plusSeconds(3), resolver);

        savedPayment2.cancel(FIXED_CLOCK.instant().plusSeconds(2), resolver);

        savedPayment3.start(FIXED_CLOCK.instant().plusSeconds(1), resolver);
        savedPayment4.start(FIXED_CLOCK.instant().plusSeconds(4), resolver);

        savedPayment1.complete(FIXED_CLOCK.instant().plusSeconds(16), resolver);
        savedPayment4.complete(FIXED_CLOCK.instant().plusSeconds(15), resolver);

        paymentRepository.save(savedPayment1);
        paymentRepository.save(savedPayment2);
        paymentRepository.save(savedPayment3);
        paymentRepository.save(savedPayment4);
        clearPersistenceContext();

        Money calculatedAmount = paymentRepository.calculatePaidAmount(INVOICE_ID);

        BigDecimal totalCompletedAmount = new BigDecimal(amount1).add(new BigDecimal(amount4));

        assertNotNull(calculatedAmount);
        assertEquals(0, Money.of(totalCompletedAmount, CURRENCY_EUR).compareTo(calculatedAmount));
    }

    @Test
    void should_include_only_payments_of_requested_invoice_in_paid_amount() {

        PaymentStateResolver resolver = new PaymentStateResolver();

        String amount1 = "25.75";
        String amount2 = "100.1";
        String amount3 = "46.32000";
        Long otherInvoiceId = 9999L;

        Payment payment1 = getCreatedPayment(INVOICE_ID, amount1);
        Payment payment2 = getCreatedPayment(otherInvoiceId, amount2);
        Payment payment3 = getCreatedPayment(INVOICE_ID, amount3);

        Payment savedPayment1 = paymentRepository.save(payment1);
        Payment savedPayment2 = paymentRepository.save(payment2);
        Payment savedPayment3 = paymentRepository.save(payment3);
        clearPersistenceContext();

        savedPayment1.start(FIXED_CLOCK.instant().plusSeconds(3), resolver);
        savedPayment2.start(FIXED_CLOCK.instant().plusSeconds(2), resolver);
        savedPayment3.start(FIXED_CLOCK.instant().plusSeconds(1), resolver);
        savedPayment1.complete(FIXED_CLOCK.instant().plusSeconds(6), resolver);
        savedPayment2.complete(FIXED_CLOCK.instant().plusSeconds(5), resolver);
        savedPayment3.complete(FIXED_CLOCK.instant().plusSeconds(4), resolver);
        paymentRepository.save(savedPayment1);
        paymentRepository.save(savedPayment2);
        paymentRepository.save(savedPayment3);
        clearPersistenceContext();

        Money calculatedAmount = paymentRepository.calculatePaidAmount(INVOICE_ID);

        BigDecimal totalAmount = new BigDecimal(amount1).add(new BigDecimal(amount3));

        assertNotNull(calculatedAmount);
        assertEquals(0, Money.of(totalAmount, CURRENCY_EUR).compareTo(calculatedAmount));
    }

    @Test
    void should_return_zero_amount_when_payments_belong_to_other_invoice() {

        PaymentStateResolver resolver = new PaymentStateResolver();

        String amount1 = "25.75";
        String amount2 = "100.1";
        String amount3 = "46.32000";
        Long otherInvoiceId = 9999L;

        Payment payment1 = getCreatedPayment(otherInvoiceId, amount1);
        Payment payment2 = getCreatedPayment(otherInvoiceId, amount2);
        Payment payment3 = getCreatedPayment(otherInvoiceId, amount3);

        Payment savedPayment1 = paymentRepository.save(payment1);
        Payment savedPayment2 = paymentRepository.save(payment2);
        Payment savedPayment3 = paymentRepository.save(payment3);
        clearPersistenceContext();

        savedPayment1.start(FIXED_CLOCK.instant().plusSeconds(3), resolver);
        savedPayment2.start(FIXED_CLOCK.instant().plusSeconds(2), resolver);
        savedPayment3.start(FIXED_CLOCK.instant().plusSeconds(1), resolver);
        savedPayment1.complete(FIXED_CLOCK.instant().plusSeconds(6), resolver);
        savedPayment2.complete(FIXED_CLOCK.instant().plusSeconds(5), resolver);
        savedPayment3.complete(FIXED_CLOCK.instant().plusSeconds(4), resolver);
        paymentRepository.save(savedPayment1);
        paymentRepository.save(savedPayment2);
        paymentRepository.save(savedPayment3);
        clearPersistenceContext();

        Money calculatedAmount = paymentRepository.calculatePaidAmount(INVOICE_ID);

        assertNotNull(calculatedAmount);
        assertEquals(0, Money.zero(CURRENCY_EUR).compareTo(calculatedAmount));
    }

    @Test
    void should_return_amount_dto_list_when_payments_have_different_currencies() {
        PaymentStateResolver resolver = new PaymentStateResolver();

        String amount1 = "25.75";
        String amount2 = "100.1";
        String amount3 = "46.32000";

        String amount4 = "584.87000000";
        String amount5 = "51.75";

        Payment payment1 = getCreatedPayment(INVOICE_ID, amount1);
        Payment payment2 = getCreatedPayment(INVOICE_ID, amount2);
        Payment payment3 = getCreatedPayment(INVOICE_ID, amount3);
        Payment payment4 = new Payment(
                INVOICE_ID,
                Money.of(amount4, CURRENCY_USD),
                METHOD,
                FIXED_CLOCK.instant());

        Payment payment5 = new Payment(
                INVOICE_ID,
                Money.of(amount5, CURRENCY_USD),
                METHOD,
                FIXED_CLOCK.instant());

        Payment savedPayment1 = paymentRepository.save(payment1);
        Payment savedPayment2 = paymentRepository.save(payment2);
        Payment savedPayment3 = paymentRepository.save(payment3);
        Payment savedPayment4 = paymentRepository.save(payment4);
        Payment savedPayment5 = paymentRepository.save(payment5);
        clearPersistenceContext();

        savedPayment1.start(FIXED_CLOCK.instant().plusSeconds(3), resolver);
        savedPayment2.start(FIXED_CLOCK.instant().plusSeconds(2), resolver);
        savedPayment3.start(FIXED_CLOCK.instant().plusSeconds(1), resolver);
        savedPayment4.start(FIXED_CLOCK.instant().plusSeconds(4), resolver);
        savedPayment5.start(FIXED_CLOCK.instant().plusSeconds(5), resolver);

        savedPayment1.complete(FIXED_CLOCK.instant().plusSeconds(16), resolver);
        savedPayment2.complete(FIXED_CLOCK.instant().plusSeconds(12), resolver);
        savedPayment3.complete(FIXED_CLOCK.instant().plusSeconds(14), resolver);
        savedPayment4.complete(FIXED_CLOCK.instant().plusSeconds(15), resolver);
        savedPayment5.complete(FIXED_CLOCK.instant().plusSeconds(18), resolver);

        paymentRepository.save(savedPayment1);
        paymentRepository.save(savedPayment2);
        paymentRepository.save(savedPayment3);
        paymentRepository.save(savedPayment4);
        paymentRepository.save(savedPayment5);
        clearPersistenceContext();

        List<AmountDto> amountDtoList = jpaPaymentRepository
                .calculatePaidAmountPerCurrency(INVOICE_ID,
                        PaymentStatus.COMPLETED);

        BigDecimal totalEurAmount = new BigDecimal(amount1)
                .add(new BigDecimal(amount2))
                .add(new BigDecimal(amount3));

        BigDecimal totalUsdAmount = new BigDecimal(amount4)
                .add(new BigDecimal(amount5));

        assertNotNull(amountDtoList);
        assertEquals(2, amountDtoList.size());

        String eurCode = CURRENCY_EUR.getCurrencyCode();
        String usdCode = CURRENCY_USD.getCurrencyCode();

        Set<String> currencies = amountDtoList.stream()
                .map(AmountDto::currency)
                .map(String::strip)
                .collect(Collectors.toSet());

        assertEquals(Set.of(eurCode, usdCode), currencies);

        amountDtoList.forEach(a -> {

            if (usdCode.equals(a.currency())) {
                assertEquals(0, totalUsdAmount.compareTo(a.amount()));
            }
            if (eurCode.equals(a.currency())) {
                assertEquals(0, totalEurAmount.compareTo(a.amount()));
            }
        });
    }

    @Test
    void should_return_empty_amount_dto_list_when_payments_belong_to_other_invoice() {

        PaymentStateResolver resolver = new PaymentStateResolver();

        String amount1 = "25.75";
        String amount2 = "100.1";
        String amount3 = "46.32000";
        Long otherInvoiceId = 9999L;

        Payment payment1 = getCreatedPayment(otherInvoiceId, amount1);
        Payment payment2 = getCreatedPayment(otherInvoiceId, amount2);
        Payment payment3 = getCreatedPayment(otherInvoiceId, amount3);

        Payment savedPayment1 = paymentRepository.save(payment1);
        Payment savedPayment2 = paymentRepository.save(payment2);
        Payment savedPayment3 = paymentRepository.save(payment3);
        clearPersistenceContext();

        savedPayment1.start(FIXED_CLOCK.instant().plusSeconds(3), resolver);
        savedPayment2.start(FIXED_CLOCK.instant().plusSeconds(2), resolver);
        savedPayment3.start(FIXED_CLOCK.instant().plusSeconds(1), resolver);
        savedPayment1.complete(FIXED_CLOCK.instant().plusSeconds(6), resolver);
        savedPayment2.complete(FIXED_CLOCK.instant().plusSeconds(5), resolver);
        savedPayment3.complete(FIXED_CLOCK.instant().plusSeconds(4), resolver);
        paymentRepository.save(savedPayment1);
        paymentRepository.save(savedPayment2);
        paymentRepository.save(savedPayment3);
        clearPersistenceContext();

        List<AmountDto> amountDtoList = jpaPaymentRepository
                .calculatePaidAmountPerCurrency(INVOICE_ID,
                        PaymentStatus.COMPLETED);

        assertNotNull(amountDtoList);
        assertTrue(amountDtoList.isEmpty());
    }

    private PaymentEntity getEntityWithStatusCreated() {

        PaymentEntity entity = new PaymentEntity();

        entity.setInvoiceId(INVOICE_ID);
        entity.setAmount(AMOUNT.amount());
        entity.setCurrency(CURRENCY_EUR.getCurrencyCode());
        entity.setStatus(PaymentStatus.CREATED);
        entity.setMethod(METHOD);
        entity.setCreatedAt(FIXED_CLOCK.instant());

        return entity;
    }

    private Payment getCreatedPayment(Long invoiceId, String amount) {

        Money money = Money.of(amount, CURRENCY_EUR);

        return new Payment(
                invoiceId,
                money,
                METHOD,
                FIXED_CLOCK.instant());
    }

    private Payment getCreatedPayment() {

        return getCreatedPayment(INVOICE_ID, AMOUNT.amount().toString());
    }

    private Payment getCreatedPayment(String amount) {

        return getCreatedPayment(INVOICE_ID, amount);
    }

    private Payment getCreatedPayment(Long invoiceId) {

        return getCreatedPayment(invoiceId, AMOUNT.amount().toString());
    }

    private void clearPersistenceContext() {
        jpaPaymentRepository.flush();
        entityManager.clear();
    }
}
