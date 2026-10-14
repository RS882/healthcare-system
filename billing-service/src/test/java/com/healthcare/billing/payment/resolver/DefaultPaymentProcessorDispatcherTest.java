package com.healthcare.billing.payment.resolver;

import com.healthcare.billing.exception.DispatcherValidationException;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.command.CardPaymentCompletionCommand;
import com.healthcare.billing.payment.command.CashPaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.payment_method_details.CashPaymentMethodDetails;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;
import com.healthcare.billing.payment.processor.PaymentProcessor;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class DefaultPaymentProcessorDispatcherTest {

    @Mock
    private PaymentProcessor<CashPaymentCompletionCommand, CashPaymentMethodDetails> cashProcessor;

    @Mock
    private PaymentProcessor<CashPaymentCompletionCommand, CashPaymentMethodDetails> duplicateCashProcessor;

    @Mock
    private PaymentProcessor<CardPaymentCompletionCommand, PaymentMethodDetails> cardProcessor;

    @Mock
    private Payment payment;

    @Mock
    private CashPaymentMethodDetails cashDetails;

    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void should_delegate_to_cash_processor() {

        DefaultPaymentProcessorDispatcher dispatcher = createValidDispatcher();

        CashPaymentCompletionCommand command = createValidCommand();

        doCallRealMethod()
                .when(cashProcessor)
                .processChecked(any(), any());

        when(cashProcessor.process(payment, command)).thenReturn(cashDetails);

        PaymentMethodDetails details = dispatcher.process(payment, command);

        assertSame(cashDetails, details);
        verify(cashProcessor).process(payment, command);
        verify(cashProcessor, times(1)).processChecked(payment, command);
    }

    @Test
    void should_return_exception_when_payment_is_null() {

        DefaultPaymentProcessorDispatcher dispatcher = createValidDispatcher();

        CashPaymentCompletionCommand command = createValidCommand();

        assertThrows(DispatcherValidationException.class,
                () -> dispatcher.process(null, command));

        verify(cashProcessor, never())
                .processChecked(any(), any());

        verify(cashProcessor, never())
                .process(any(), any());
    }

    @Test
    void should_return_exception_when_command_is_null() {

        DefaultPaymentProcessorDispatcher dispatcher = createValidDispatcher();

        assertThrows(DispatcherValidationException.class,
                () -> dispatcher.process(payment, null));

        verify(cashProcessor, never())
                .processChecked(any(), any());

        verify(cashProcessor, never())
                .process(any(), any());
    }

    @Test
    void should_throw_exception_when_processor_is_not_registered() {

        CardPaymentCompletionCommand command =
                new CardPaymentCompletionCommand(
                        2L,
                        "card-transaction-123"
                );

        DefaultPaymentProcessorDispatcher dispatcher = createValidDispatcher();

        assertThrows(DispatcherValidationException.class,
                () -> dispatcher.process(payment, command));

        verify(cashProcessor, never())
                .processChecked(any(), any());
    }

    @Test
    void should_throw_exception_when_processor_list_is_null() {

        assertThrows(IllegalStateException.class,
                () -> new DefaultPaymentProcessorDispatcher(null));

    }

    @Test
    void should_throw_exception_when_processor_list_is_empty() {

        assertThrows(IllegalStateException.class,
                () -> new DefaultPaymentProcessorDispatcher(List.of()));

    }

    @Test
    void should_throw_exception_when_processors_in_list_is_null() {

        assertThrows(IllegalStateException.class,
                () -> new DefaultPaymentProcessorDispatcher(Arrays.asList(null, cashProcessor)));

    }

    @Test
    void should_throw_exception_when_supported_command_type_is_null() {

        when(cashProcessor.supportedCommandType())
                .thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> new DefaultPaymentProcessorDispatcher(List.of(cashProcessor)));

    }

    @Test
    void should_throw_exception_when_processors_have_duplicate_command_types() {

        when(cashProcessor.supportedCommandType())
                .thenReturn(CashPaymentCompletionCommand.class);

        when(duplicateCashProcessor.supportedCommandType())
                .thenReturn(CashPaymentCompletionCommand.class);

        assertThrows(IllegalStateException.class,
                () -> new DefaultPaymentProcessorDispatcher(List.of(cashProcessor, duplicateCashProcessor)));

    }

    @Test
    void should_delegate_only_to_matching_processor_when_multiple_registered() {

        CashPaymentCompletionCommand command = createValidCommand();

        when(cashProcessor.supportedCommandType())
                .thenReturn(CashPaymentCompletionCommand.class);

        when(cardProcessor.supportedCommandType())
                .thenReturn(CardPaymentCompletionCommand.class);

        DefaultPaymentProcessorDispatcher dispatcher =
                new DefaultPaymentProcessorDispatcher(
                        List.of(cashProcessor, cardProcessor)
                );

        doCallRealMethod()
                .when(cashProcessor)
                .processChecked(any(), any());

        when(cashProcessor.process(payment, command))
                .thenReturn(cashDetails);

        PaymentMethodDetails result = dispatcher.process(payment, command);

        assertSame(cashDetails, result);

        verify(cashProcessor, times(1))
                .processChecked(payment, command);

        verify(cashProcessor, times(1))
                .process(payment, command);

        verify(cardProcessor, never())
                .processChecked(any(), any());

        verify(cardProcessor, never())
                .process(any(), any());
    }

    private DefaultPaymentProcessorDispatcher createValidDispatcher() {

        when(cashProcessor.supportedCommandType())
                .thenReturn(CashPaymentCompletionCommand.class);

        return new DefaultPaymentProcessorDispatcher(List.of(cashProcessor));
    }

    private CashPaymentCompletionCommand createValidCommand() {
        return new CashPaymentCompletionCommand(
                2L,
                Money.of("350.00", EUR)
        );
    }
}