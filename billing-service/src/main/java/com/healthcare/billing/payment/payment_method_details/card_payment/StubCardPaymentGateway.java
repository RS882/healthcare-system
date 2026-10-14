package com.healthcare.billing.payment.payment_method_details.card_payment;

import com.healthcare.billing.exception.CardPaymentGatewayException;
import com.healthcare.billing.exception.CardPaymentServerUnavailableException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentVerificationStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Currency;
import java.util.Map;

@Profile("stub")
@Service
public class StubCardPaymentGateway implements CardPaymentGateway {

    private static final Currency EUR = Currency.getInstance("EUR");

    private static final Map<String, StubCardPaymentResult> stubResult = Map.of(

            "SUCCEEDED_ID#1", new StubCardPaymentResult(Money.of("256.91", EUR),
                    StubCardPaymentStatus.SUCCEEDED),

            "PENDING_ID#2", new StubCardPaymentResult(Money.of("10.5", EUR),
                    StubCardPaymentStatus.PENDING),

            "FAILED_ID#3", new StubCardPaymentResult(Money.of("870.28", EUR),
                    StubCardPaymentStatus.FAILED),

            "ERROR_ID#4", new StubCardPaymentResult(Money.of("2637.01", EUR),
                    StubCardPaymentStatus.ERROR)
    );

    @Override
    public CardPaymentVerificationResult verifyTransaction(String providerTransactionId) {

        validateProviderTransactionId(providerTransactionId);

        StubCardPaymentResult result = stubResult.get(providerTransactionId);

        if (result == null) {
            throw new CardPaymentGatewayException(
                    "Provider transaction id < %s > not found".formatted(providerTransactionId));
        }

        return new CardPaymentVerificationResult(
                providerTransactionId,
                resolveStatus(result.status),
                result.amount
        );
    }

    private CardPaymentVerificationStatus resolveStatus(StubCardPaymentStatus stubCardPaymentStatus) {
        return switch (stubCardPaymentStatus) {
            case SUCCEEDED -> CardPaymentVerificationStatus.SUCCEEDED;
            case FAILED -> CardPaymentVerificationStatus.FAILED;
            case PENDING -> CardPaymentVerificationStatus.PENDING;
            case ERROR -> throw new CardPaymentServerUnavailableException("Card payment gateway unavailable");
        };
    }

    private void validateProviderTransactionId(String providerTransactionId) {
        if (!StringUtils.hasText(providerTransactionId)) {
            throw new CardPaymentGatewayException("Provider transaction ID is null or blank");
        }
    }

    private record StubCardPaymentResult(
            Money amount,
            StubCardPaymentStatus status
    ) {
    }

    private enum StubCardPaymentStatus {
        SUCCEEDED, PENDING, FAILED, ERROR
    }
}
