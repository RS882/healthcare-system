package com.healthcare.billing.model.entity;

import com.healthcare.billing.model.value.Money;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InvoiceItem {

    private Long id;

    private String description;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    /**
     * Tax rate as decimal fraction.
     *
     * Examples:
     * 19%  -> 0.19
     * 7%   -> 0.07
     * 7.5% -> 0.075
     */
    private BigDecimal taxRate;

    private Money netAmount;

    private Money taxAmount;

    private Money totalAmount;
}