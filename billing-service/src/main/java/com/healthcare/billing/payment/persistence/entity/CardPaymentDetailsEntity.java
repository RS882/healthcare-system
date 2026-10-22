package com.healthcare.billing.payment.persistence.entity;

import com.healthcare.billing.payment.payment_method_details.card_payment.enums.CardPaymentProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card_payment_details")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CardPaymentDetailsEntity {

    @Id
    @Column(
            name = "payment_id",
            updatable = false
    )
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "provider",
            nullable = false,
            length = 50,
            updatable = false
    )
    private CardPaymentProvider provider;

    @Column(
            name = "provider_transaction_id",
            nullable = false,
            updatable = false
    )
    private String providerTransactionId;
}
