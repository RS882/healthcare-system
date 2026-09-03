package com.healthcare.billing.generator;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SimpleInvoiceNumberGenerator implements InvoiceNumberGenerator {

    private final AtomicLong sequence = new AtomicLong();

    private final Clock clock;

    public SimpleInvoiceNumberGenerator(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String nextNumber() {

        long number = sequence.incrementAndGet();
        int year = LocalDate.now(clock).getYear();

        return "INV-%d-%06d".formatted(year, number);
    }
}