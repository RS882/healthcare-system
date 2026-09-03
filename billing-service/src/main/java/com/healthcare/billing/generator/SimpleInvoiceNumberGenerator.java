package com.healthcare.billing.generator;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SimpleInvoiceNumberGenerator implements InvoiceNumberGenerator {

    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public String nextNumber() {

        long number = sequence.incrementAndGet();

        return "INV-%d-%06d".formatted(
                LocalDate.now().getYear(),
                number
        );
    }
}
