package com.healthcare.billing.payment.persistence.mapper;

import com.healthcare.billing.exception.PaymentDetailsValidationException;
import com.healthcare.billing.payment.payment_method_details.CardPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import com.healthcare.billing.payment.persistence.entity.CardPaymentDetailsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CardPaymentDetailsMapperTest {

    private CardPaymentDetailsMapper mapper;

    private static final Long PAYMENT_ID = 97L;
    private static final CardPaymentProvider PROVIDER = CardPaymentProvider.ADYEN;
    private static final String PROVIDER_TRANSACTION_ID = "Test transaction ID";


    @BeforeEach
    void setUp() {
        mapper = new CardPaymentDetailsMapper();
    }

    @Test
    void toEntity_should_map_all_fields() {

        CardPaymentMethodDetails details =
                new CardPaymentMethodDetails(PROVIDER_TRANSACTION_ID, PROVIDER);

        CardPaymentDetailsEntity entity = mapper.toEntity(details, PAYMENT_ID);

        assertNotNull(entity);
        assertEquals(PROVIDER_TRANSACTION_ID, entity.getProviderTransactionId());
        assertEquals(PROVIDER, entity.getProvider());
        assertEquals(PAYMENT_ID, entity.getPaymentId());
    }

    @Test
    void toDomain_should_map_all_fields() {

        CardPaymentDetailsEntity entity = new CardPaymentDetailsEntity(
                PAYMENT_ID,
                PROVIDER,
                PROVIDER_TRANSACTION_ID
        );

        CardPaymentMethodDetails details = mapper.toDomain(entity);

        assertNotNull(details);
        assertEquals(PROVIDER, details.provider());
        assertEquals(PROVIDER_TRANSACTION_ID, details.providerTransactionId());
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
    void toEntity_should_reject_null_or_invalid_payment_id(Long id) {

        CardPaymentMethodDetails details =
                new CardPaymentMethodDetails(PROVIDER_TRANSACTION_ID, PROVIDER);

        assertThrows(PaymentDetailsValidationException.class,
                () -> mapper.toEntity(details, id));
    }

    @Test
    void toDomain_should_reject_null_entity() {

        assertThrows(PaymentDetailsValidationException.class,
                () -> mapper.toDomain(null));
    }
}