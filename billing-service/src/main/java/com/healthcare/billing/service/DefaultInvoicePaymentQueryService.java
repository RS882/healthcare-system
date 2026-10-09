package com.healthcare.billing.service;

import com.healthcare.billing.exception.InvoiceValidationException;
import com.healthcare.billing.exception.PaymentBalanceValidationException;
import com.healthcare.billing.model.entity.invoice.Invoice;
import com.healthcare.billing.model.value.Money;
import com.healthcare.billing.payment.model.InvoicePaymentSummary;
import com.healthcare.billing.payment.model.PaymentBalance;
import com.healthcare.billing.payment.model.enums.PaymentCoverageStatus;
import com.healthcare.billing.payment.repository.PaymentRepository;
import com.healthcare.billing.persistence.InvoiceStore;
import com.healthcare.billing.service.interfaces.InvoicePaymentQueryService;
import com.healthcare.billing.service.interfaces.PaymentBalanceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DefaultInvoicePaymentQueryService implements InvoicePaymentQueryService {

    private final InvoiceStore invoiceStore;
    private final PaymentRepository paymentRepository;
    private final PaymentBalanceCalculator paymentBalanceCalculator;


    @Override
    @Transactional(readOnly = true)
    public InvoicePaymentSummary getPaymentSummary(Long invoiceId) {

        validateId(invoiceId);

        Invoice invoice = invoiceStore.findById(invoiceId);

        Money paidAmount = paymentRepository.calculatePaidAmount(invoiceId);

        return resolveInvoicePaymentSummary(invoice, paidAmount);
    }

    private InvoicePaymentSummary resolveInvoicePaymentSummary(Invoice invoice, Money paidAmount) {

        Money totalAmount = invoice.getTotalAmount();

        PaymentBalance balance = paymentBalanceCalculator
                .calculateInvoicePaymentBalance(totalAmount, paidAmount);

        PaymentCoverageStatus paymentCoverageStatus = resolvePaymentCoverageStatus(paidAmount, balance);

        return InvoicePaymentSummary.builder()
                .invoiceId(invoice.getId())
                .invoiceStatus(invoice.getStatus())
                .paymentCoverageStatus(paymentCoverageStatus)
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .balance(balance)
                .build();

    }

    private PaymentCoverageStatus resolvePaymentCoverageStatus(
            Money paidAmount, PaymentBalance balance) {

        if (paidAmount.isZero()) {
            return PaymentCoverageStatus.UNPAID;

        }
        boolean isRemainingAmountZero = balance.remainingAmount().isZero();
        boolean isOverpaidAmountZero = balance.overpaidAmount().isZero();

        if (isRemainingAmountZero && isOverpaidAmountZero) {
            return PaymentCoverageStatus.FULLY_PAID;
        }

        if (isRemainingAmountZero && !isOverpaidAmountZero) {
            return PaymentCoverageStatus.OVERPAID;
        }
        if (!isRemainingAmountZero && isOverpaidAmountZero) {
            return PaymentCoverageStatus.PARTIALLY_PAID;
        }
        throw new PaymentBalanceValidationException("Incorrect payment balance");

    }

    private void validateId(Long id) {
        if (id == null) {
            throw new InvoiceValidationException("Invoice id cannot be null");
        }
        if (id <= 0) {
            throw new InvoiceValidationException("Invoice id must be greater than zero");
        }
    }
}
