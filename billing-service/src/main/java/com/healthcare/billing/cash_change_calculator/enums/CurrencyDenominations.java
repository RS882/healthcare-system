package com.healthcare.billing.cash_change_calculator.enums;

import com.healthcare.billing.exception.CurrencyNominalException;
import com.healthcare.billing.exception.CurrencyNominalIndexException;
import com.healthcare.billing.exception.UnsupportedCurrencyException;

import java.util.Arrays;

public enum CurrencyDenominations {

    EUR(true,
            500,
            "cent",
            new long[]{
                    20000, 10000, 5000, 2000, 1000, 500,
                    200, 100, 50, 20, 10, 5, 2, 1
            }),
    USD(false,
            100,
            "cent",
            new long[]{
                    10000, 5000, 2000, 1000, 500,
                    200, 100, 50, 25, 10, 5, 1
            });

    private final boolean supported;
    private final long lowestBanknote;
    private final String fractionalUnit;
    private final long[] nominals;

    CurrencyDenominations(
            boolean supported,
            long lowestBanknote,
            String fractionalUnit,
            long[] nominals) {
        this.supported = supported;
        this.lowestBanknote = lowestBanknote;
        this.fractionalUnit = fractionalUnit;
        this.nominals = nominals;
    }

    public long[] getNominals() {
        validateSupported();
        return nominals.clone();
    }

    public String getFractionalUnit() {
        validateSupported();
        return fractionalUnit;
    }

    public long getLowestBanknote() {
        validateSupported();
        return lowestBanknote;
    }

    public String getNominalAsStringByIndex(int i) {
        validateSupported();

        if (i < 0 || i >= nominals.length) {
            throw new CurrencyNominalIndexException(i);
        }

        long nominal = nominals[i];

        if (nominal >= 100) {
            return "%d %s".formatted(nominal / 100, name());
        }

        return "%d %s".formatted(nominal, fractionalUnit);
    }

    public boolean isBanknote(long nominal) {
        validateSupported();
        validateNominal(nominal);

        return nominal >= lowestBanknote;
    }

    private void validateNominal(long nominal) {
        if (nominal <= 0) {
            throw new CurrencyNominalException("Nominal must be greater than zero: %d %s"
                    .formatted(nominal, fractionalUnit));
        }

        if (Arrays.stream(nominals).noneMatch(value -> value == nominal)) {
            throw new CurrencyNominalException("Unsupported nominal: %d %s for currency %s"
                    .formatted(nominal, fractionalUnit, name()));
        }
    }

    private void validateSupported() {
        if (!supported) {
            throw new UnsupportedCurrencyException(name());
        }
    }
}