package com.healthcare.billing.cash_change_calculator.enums;

import com.healthcare.billing.exception.CurrencyNominalException;
import com.healthcare.billing.exception.CurrencyNominalIndexException;
import com.healthcare.billing.exception.UnsupportedCurrencyException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CurrencyDenominationsTest {

    @Test
    void should_return_all_euro_denominations_in_descending_order() {
        assertThat(CurrencyDenominations.EUR.getNominals()).containsExactly(
                20000, 10000, 5000, 2000, 1000, 500,
                200, 100, 50, 20, 10, 5, 2, 1
        );
    }

    @Test
    void should_return_copy_of_nominals_array() {
        long[] first = CurrencyDenominations.EUR.getNominals();
        long[] second = CurrencyDenominations.EUR.getNominals();

        first[0] = 1;

        assertThat(second[0]).isEqualTo(20000);
        assertThat(CurrencyDenominations.EUR.getNominals()[0]).isEqualTo(20000);
    }

    @Test
    void should_return_fractional_unit() {
        assertThat(CurrencyDenominations.EUR.getFractionalUnit())
                .isEqualTo("cent");
    }

    @Test
    void should_return_lowest_banknote() {
        assertThat(CurrencyDenominations.EUR.getLowestBanknote())
                .isEqualTo(500);
    }

    @Test
    void should_format_banknote_nominal() {
        assertThat(CurrencyDenominations.EUR.getNominalAsStringByIndex(5))
                .isEqualTo("5 EUR");
    }

    @Test
    void should_format_euro_coin_nominal() {
        assertThat(CurrencyDenominations.EUR.getNominalAsStringByIndex(6))
                .isEqualTo("2 EUR");

        assertThat(CurrencyDenominations.EUR.getNominalAsStringByIndex(7))
                .isEqualTo("1 EUR");
    }

    @Test
    void should_format_cent_coin_nominal() {
        assertThat(CurrencyDenominations.EUR.getNominalAsStringByIndex(8))
                .isEqualTo("50 cent");

        assertThat(CurrencyDenominations.EUR.getNominalAsStringByIndex(13))
                .isEqualTo("1 cent");
    }

    @Test
    void should_identify_banknote() {
        assertThat(CurrencyDenominations.EUR.isBanknote(500)).isTrue();
        assertThat(CurrencyDenominations.EUR.isBanknote(1000)).isTrue();
        assertThat(CurrencyDenominations.EUR.isBanknote(20000)).isTrue();
    }

    @Test
    void should_identify_euro_coin_as_not_banknote() {
        assertThat(CurrencyDenominations.EUR.isBanknote(200)).isFalse();
        assertThat(CurrencyDenominations.EUR.isBanknote(100)).isFalse();
    }

    @Test
    void should_identify_fractional_coin_as_not_banknote() {
        assertThat(CurrencyDenominations.EUR.isBanknote(50)).isFalse();
        assertThat(CurrencyDenominations.EUR.isBanknote(20)).isFalse();
        assertThat(CurrencyDenominations.EUR.isBanknote(1)).isFalse();
    }

    @Test
    void should_throw_exception_when_nominal_is_zero() {
        assertThatThrownBy(() -> CurrencyDenominations.EUR.isBanknote(0))
                .isInstanceOf(CurrencyNominalException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void should_throw_exception_when_nominal_is_negative() {
        assertThatThrownBy(() -> CurrencyDenominations.EUR.isBanknote(-500))
                .isInstanceOf(CurrencyNominalException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void should_throw_exception_when_nominal_is_not_supported_for_currency() {
        assertThatThrownBy(() -> CurrencyDenominations.EUR.isBanknote(777))
                .isInstanceOf(CurrencyNominalException.class)
                .hasMessageContaining("777")
                .hasMessageContaining("EUR");
    }

    @Test
    void should_throw_exception_when_nominal_index_is_negative() {
        assertThatThrownBy(() -> CurrencyDenominations.EUR.getNominalAsStringByIndex(-1))
                .isInstanceOf(CurrencyNominalIndexException.class);
    }

    @Test
    void should_throw_exception_when_nominal_index_is_equal_to_array_length() {
        int invalidIndex = CurrencyDenominations.EUR.getNominals().length;

        assertThatThrownBy(() -> CurrencyDenominations.EUR.getNominalAsStringByIndex(invalidIndex))
                .isInstanceOf(CurrencyNominalIndexException.class);
    }

    @Test
    void should_throw_unsupported_currency_exception_when_getting_nominals_for_unsupported_currency() {
        assertThatThrownBy(() -> CurrencyDenominations.USD.getNominals())
                .isInstanceOf(UnsupportedCurrencyException.class);
    }

    @Test
    void should_throw_unsupported_currency_exception_when_getting_fractional_unit_for_unsupported_currency() {
        assertThatThrownBy(() -> CurrencyDenominations.USD.getFractionalUnit())
                .isInstanceOf(UnsupportedCurrencyException.class);
    }

    @Test
    void should_throw_unsupported_currency_exception_when_getting_lowest_banknote_for_unsupported_currency() {
        assertThatThrownBy(() -> CurrencyDenominations.USD.getLowestBanknote())
                .isInstanceOf(UnsupportedCurrencyException.class);
    }

    @Test
    void should_throw_unsupported_currency_exception_when_getting_nominal_string_for_unsupported_currency() {
        assertThatThrownBy(() -> CurrencyDenominations.USD.getNominalAsStringByIndex(0))
                .isInstanceOf(UnsupportedCurrencyException.class);
    }

    @Test
    void should_throw_unsupported_currency_exception_when_checking_banknote_for_unsupported_currency() {
        assertThatThrownBy(() -> CurrencyDenominations.USD.isBanknote(100))
                .isInstanceOf(UnsupportedCurrencyException.class);
    }
}