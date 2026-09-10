package com.healthcare.billing.cash_change_calculator;

import com.healthcare.billing.cash_change_calculator.enums.CurrencyDenominations;
import com.healthcare.billing.exception.CashChangeCalculatorException;
import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.exception.UnsupportedCurrencyException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.money.MoneyPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CashChangeCalculator {

    private final MoneyPolicy moneyPolicy;

    public ChangeBreakdown calculate(Money totalAmount, Money receivedAmount) {
        Currency currentCurrency = moneyPolicy.currency();

        validateAmounts(totalAmount, receivedAmount, currentCurrency);
        checkReceivedAmount(totalAmount, receivedAmount);

        Money change = receivedAmount.subtract(totalAmount);

        return change.isZero() ?
                new ChangeBreakdown(Money.zero(currentCurrency), Map.of())
                : new ChangeBreakdown(change, getDenominations(change, currentCurrency));
    }

    private Map<String, Long> getDenominations(Money change, Currency currency) {

        CurrencyDenominations nominal = getCurrencyNominal(currency);

        long[] nominals = nominal.getNominals();

        long remainingAmount = toFractionalUnits(change, currency);

        Map<String, Long> denominations = new LinkedHashMap<>();

        for (int i = 0; i < nominals.length && remainingAmount > 0; i++) {
            long currentNominal = nominals[i];
            long count = remainingAmount / currentNominal;

            if (count > 0) {
                denominations.put(nominal.getNominalAsStringByIndex(i), count);
                remainingAmount %= currentNominal;
            }
        }
        return denominations;
    }

    private long toFractionalUnits(Money change, Currency currency) {
        try {
            return change.amount()
                    .movePointRight(currency.getDefaultFractionDigits())
                    .longValueExact();
        } catch (ArithmeticException e) {
            throw new CashChangeCalculatorException(
                    "Amount cannot be represented in the smallest currency unit: %s"
                            .formatted(change));
        }
    }

    private CurrencyDenominations getCurrencyNominal(Currency currency) {

        String currencyName = currency.getCurrencyCode();

        try {
            return CurrencyDenominations.valueOf(currencyName);

        } catch (IllegalArgumentException e) {
            throw new UnsupportedCurrencyException(currencyName);
        }
    }

    private void checkReceivedAmount(Money totalAmount, Money receivedAmount) {
        if (receivedAmount.compareTo(totalAmount) < 0) {
            Money shortfall = totalAmount.subtract(receivedAmount);

            throw new CashChangeCalculatorException(
                    "Received amount must be greater than or equal to total amount. Shortfall: %s"
                            .formatted(shortfall));
        }
    }

    private void validateAmounts(Money totalAmount, Money receivedAmount, Currency currentCurrency) {
        validateAmount(totalAmount, currentCurrency);
        validateAmount(receivedAmount, currentCurrency);
    }

    private void validateAmount(Money amount, Currency currentCurrency) {

        if (amount == null) {
            throw new CashChangeCalculatorException("Amount must not be null");
        }

        if (!amount.isPositive()) {
            throw new CashChangeCalculatorException("Amount must be greater than zero");
        }
        Currency amountCurrency = amount.currency();
        if (!currentCurrency.equals(amountCurrency)) {
            throw new CurrencyMismatchException(currentCurrency, amountCurrency);
        }
    }
}
