package com.healthcare.billing.payment.resolver;

import com.healthcare.billing.exception.DispatcherValidationException;
import com.healthcare.billing.payment.command.PaymentCompletionCommand;
import com.healthcare.billing.payment.model.Payment;
import com.healthcare.billing.payment.payment_method_details.PaymentMethodDetails;
import com.healthcare.billing.payment.processor.PaymentProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DefaultPaymentProcessorDispatcher
        implements PaymentProcessorDispatcher {

    private final Map<
            Class<? extends PaymentCompletionCommand>,
            PaymentProcessor<?, ?>
            > processors;

    public DefaultPaymentProcessorDispatcher(
            List<PaymentProcessor<?, ?>> processors
    ) {

        validateProcessorList(processors);

        this.processors = processors.stream()
                .collect(Collectors.toMap(
                        PaymentProcessor::supportedCommandType,
                        processor -> processor,
                        (existing, duplicate) -> {
                            throw new IllegalStateException(
                                    "Duplicate payment processors: %s and %s"
                                            .formatted(
                                                    existing.getClass().getName(),
                                                    duplicate.getClass().getName()
                                            )
                            );
                        }
                ));
    }

    @Override
    public PaymentMethodDetails process(Payment payment, PaymentCompletionCommand command) {

        validateProcessArgs(payment, command);

        PaymentProcessor<?, ?> processor = processors.get(command.getClass());

        validateProcessor(processor, command);

        return processor.processChecked(payment, command);
    }

    private static void validateProcessorList(List<PaymentProcessor<?, ?>> processors) {
        if (processors == null) {
            throw new IllegalStateException("Payment processor list must not be null");
        }

        if (processors.isEmpty()) {
            throw new IllegalStateException("Payment processor list must not be empty");
        }

        for (PaymentProcessor<?, ?> processor : processors) {
            if (processor == null) {
                throw new IllegalStateException(
                        "Payment processor list contains null"
                );
            }

            if (processor.supportedCommandType() == null) {
                throw new IllegalStateException(
                        "Processor has null supported command type: %s".formatted(
                                processor.getClass().getName())
                );
            }
        }
    }

    private static void validateProcessArgs(Payment payment, PaymentCompletionCommand command) {

        if (payment == null) {
            throw new DispatcherValidationException("Payment must be not null");
        }

        if (command == null) {
            throw new DispatcherValidationException("Payment completion command must be not null");
        }
    }

    private static void validateProcessor(PaymentProcessor<?, ?> processor, PaymentCompletionCommand command) {

        if (processor == null) {
            throw new DispatcherValidationException(
                    "No payment processor registered for command: %s"
                            .formatted(command.getClass().getName())
            );
        }
    }
}


