package com.healthcare.billing.cash_change_calculator;

import com.healthcare.billing.exception.CashChangeCalculatorException;
import com.healthcare.billing.exception.CurrencyMismatchException;
import com.healthcare.billing.exception.UnsupportedCurrencyException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.money.MoneyPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashChangeCalculatorTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private MoneyPolicy moneyPolicy;

    private CashChangeCalculator calculator;

    @BeforeEach
    void set_up() {
        calculator = new CashChangeCalculator(moneyPolicy);
    }

    @Test
    void should_calculate_change_using_highest_denominations_first() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("63.27", EUR);
        Money receivedAmount = Money.of("100.00", EUR);

        ChangeBreakdown result = calculator.calculate(totalAmount, receivedAmount);

        assertThat(result.change()).isEqualTo(Money.of("36.73", EUR));

        assertThat(result.denominations().keySet()).containsExactly(
                "20 EUR",
                "10 EUR",
                "5 EUR",
                "1 EUR",
                "50 cent",
                "20 cent",
                "2 cent",
                "1 cent"
        );

        assertThat(result.denominations())
                .containsEntry("20 EUR", 1L)
                .containsEntry("10 EUR", 1L)
                .containsEntry("5 EUR", 1L)
                .containsEntry("1 EUR", 1L)
                .containsEntry("50 cent", 1L)
                .containsEntry("20 cent", 1L)
                .containsEntry("2 cent", 1L)
                .containsEntry("1 cent", 1L);
    }

    @Test
    void should_use_maximum_possible_count_of_each_high_nominal() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("1.00", EUR);
        Money receivedAmount = Money.of("200.00", EUR);

        ChangeBreakdown result = calculator.calculate(totalAmount, receivedAmount);

        assertThat(result.change()).isEqualTo(Money.of("199.00", EUR));

        assertThat(result.denominations().keySet()).containsExactly(
                "100 EUR",
                "50 EUR",
                "20 EUR",
                "5 EUR",
                "2 EUR"
        );

        assertThat(result.denominations())
                .containsEntry("100 EUR", 1L)
                .containsEntry("50 EUR", 1L)
                .containsEntry("20 EUR", 2L)
                .containsEntry("5 EUR", 1L)
                .containsEntry("2 EUR", 2L);
    }

    @Test
    void should_use_multiple_banknotes_of_same_nominal() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("10.00", EUR);
        Money receivedAmount = Money.of("100.00", EUR);

        ChangeBreakdown result = calculator.calculate(totalAmount, receivedAmount);

        assertThat(result.change()).isEqualTo(Money.of("90.00", EUR));

        assertThat(result.denominations().keySet()).containsExactly(
                "50 EUR",
                "20 EUR"
        );

        assertThat(result.denominations())
                .containsEntry("50 EUR", 1L)
                .containsEntry("20 EUR", 2L);
    }

    @Test
    void should_calculate_change_containing_only_coins() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("8.01", EUR);
        Money receivedAmount = Money.of("10.00", EUR);

        ChangeBreakdown result = calculator.calculate(totalAmount, receivedAmount);

        assertThat(result.change()).isEqualTo(Money.of("1.99", EUR));

        assertThat(result.denominations().keySet()).containsExactly(
                "1 EUR",
                "50 cent",
                "20 cent",
                "5 cent",
                "2 cent"
        );

        assertThat(result.denominations())
                .containsEntry("1 EUR", 1L)
                .containsEntry("50 cent", 1L)
                .containsEntry("20 cent", 2L)
                .containsEntry("5 cent", 1L)
                .containsEntry("2 cent", 2L);
    }

    @Test
    void should_calculate_one_cent_change() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("9.99", EUR);
        Money receivedAmount = Money.of("10.00", EUR);

        ChangeBreakdown result = calculator.calculate(totalAmount, receivedAmount);

        assertThat(result.change()).isEqualTo(Money.of("0.01", EUR));

        assertThat(result.denominations())
                .containsExactlyEntriesOf(
                        java.util.Map.of("1 cent", 1L)
                );
    }

    @Test
    void should_return_empty_denominations_when_received_amount_equals_total_amount() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("100.00", EUR);
        Money receivedAmount = Money.of("100.00", EUR);

        ChangeBreakdown result = calculator.calculate(totalAmount, receivedAmount);

        assertThat(result.change()).isEqualTo(Money.zero(EUR));
        assertThat(result.denominations()).isEmpty();
    }

    @Test
    void should_throw_exception_when_received_amount_is_less_than_total_amount() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("100.00", EUR);
        Money receivedAmount = Money.of("80.00", EUR);

        assertThatThrownBy(() -> calculator.calculate(totalAmount, receivedAmount))
                .isInstanceOf(CashChangeCalculatorException.class)
                .hasMessageContaining("Shortfall")
                .hasMessageContaining("20.00");
    }

    @Test
    void should_throw_exception_when_total_amount_is_null() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money receivedAmount = Money.of("100.00", EUR);

        assertThatThrownBy(() -> calculator.calculate(null, receivedAmount))
                .isInstanceOf(CashChangeCalculatorException.class)
                .hasMessage("Amount must not be null");
    }

    @Test
    void should_throw_exception_when_received_amount_is_null() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("50.00", EUR);

        assertThatThrownBy(() -> calculator.calculate(totalAmount, null))
                .isInstanceOf(CashChangeCalculatorException.class)
                .hasMessage("Amount must not be null");
    }

    @Test
    void should_throw_exception_when_total_amount_is_zero() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.zero(EUR);
        Money receivedAmount = Money.of("50.00", EUR);

        assertThatThrownBy(() -> calculator.calculate(totalAmount, receivedAmount))
                .isInstanceOf(CashChangeCalculatorException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    void should_throw_exception_when_received_amount_is_zero() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("50.00", EUR);
        Money receivedAmount = Money.zero(EUR);

        assertThatThrownBy(() -> calculator.calculate(totalAmount, receivedAmount))
                .isInstanceOf(CashChangeCalculatorException.class)
                .hasMessage("Amount must be greater than zero");
    }

    @Test
    void should_throw_currency_mismatch_when_total_amount_has_wrong_currency() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("50.00", USD);
        Money receivedAmount = Money.of("100.00", EUR);

        assertThatThrownBy(() -> calculator.calculate(totalAmount, receivedAmount))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void should_throw_currency_mismatch_when_received_amount_has_wrong_currency() {
        when(moneyPolicy.currency()).thenReturn(EUR);

        Money totalAmount = Money.of("50.00", EUR);
        Money receivedAmount = Money.of("100.00", USD);

        assertThatThrownBy(() -> calculator.calculate(totalAmount, receivedAmount))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void should_throw_unsupported_currency_when_currency_exists_but_is_not_supported() {
        when(moneyPolicy.currency()).thenReturn(USD);

        Money totalAmount = Money.of("50.00", USD);
        Money receivedAmount = Money.of("100.00", USD);

        assertThatThrownBy(() -> calculator.calculate(totalAmount, receivedAmount))
                .isInstanceOf(UnsupportedCurrencyException.class);
    }
}