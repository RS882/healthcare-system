package com.healthcare.billing.payment.persistence.mapper;

import com.healthcare.billing.exception.InvalidPersistedCurrencyException;
import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.persistence.entity.CashPaymentDetailsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashPaymentDetailsMapperTest {

    private CashPaymentDetailsMapper mapper;

    private static final Long PAYMENT_ID = 97L;
    private static final String EUR = "EUR";
    private static final Currency CURRENCY_EUR = Currency.getInstance(EUR);
    private static final BigDecimal AMOUNT = new BigDecimal("328.29");
    private static final Money MONEY_AMOUNT = Money.of(AMOUNT, CURRENCY_EUR);

    @BeforeEach
    void setUp() {
        mapper = new CashPaymentDetailsMapper();
    }

    @Test
    void toEntity_should_map_all_fields() {

        CashPaymentMethodDetails details = new CashPaymentMethodDetails(MONEY_AMOUNT);

        CashPaymentDetailsEntity entity = mapper.toEntity(details, PAYMENT_ID);

        assertNotNull(entity);
        assertEquals(PAYMENT_ID, entity.getPaymentId());
        assertEquals(CURRENCY_EUR.getCurrencyCode(), entity.getCurrency());
        assertEquals(AMOUNT, entity.getReceivedAmount());
    }

    @Test
    void toDomain_should_restore_received_amount() {

        CashPaymentDetailsEntity entity =
                new CashPaymentDetailsEntity(PAYMENT_ID, AMOUNT, EUR);

        CashPaymentMethodDetails details = mapper.toDomain(entity);

        assertNotNull(details);
        assertEquals(MONEY_AMOUNT, details.receivedAmount());
    }

    @Test
    void toEntity_should_reject_null_details() {

        assertThrows(PaymentDetailsValidationException.class,
                () -> mapper.toEntity(null, PAYMENT_ID));

    }

    @ParameterizedTest(name = "Test {index}: payment id [{arguments}]")
    @NullSource
    @ValueSource(longs = {
            0L,
            -726L
    })
    void toEntity_should_reject_invalid_payment_id(Long id) {

        CashPaymentMethodDetails details = new CashPaymentMethodDetails(MONEY_AMOUNT);

        assertThrows(PaymentDetailsValidationException.class,
                () -> mapper.toEntity(details, id));
    }

    @Test
    void toDomain_should_reject_null_entity() {
        assertThrows(PaymentDetailsValidationException.class,
                () -> mapper.toDomain(null));
    }

    @ParameterizedTest(name = "Test {index}: currency [{arguments}]")
    @NullAndEmptySource
    @ValueSource(strings = {
            "test",
            "     "
    })
    void toDomain_should_reject_invalid_currency(String currency) {

        CashPaymentDetailsEntity entity =
                new CashPaymentDetailsEntity(PAYMENT_ID, AMOUNT, currency);

        assertThrows(InvalidPersistedCurrencyException.class,
                () -> mapper.toDomain(entity));
    }
}