//package com.healthcare.billing.payment.persistence.entity;
//
//import com.healthcare.billing.payment.model.enums.PaymentMethod;
//import com.healthcare.billing.payment.model.enums.PaymentStatus;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//
//@Entity
//@Table(name = "payment")
//@Getter
//@Setter
//@NoArgsConstructor
//public class PaymentEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(
//            name = "invoice_id",
//            nullable = false
//    )
//    private Long invoiceId;
//
//    @Column(
//            name = "amount",
//            nullable = false,
//            precision = 19,
//            scale = 2
//    )
//    private BigDecimal amount;
//
//    @Column(
//            name = "currency",
//            nullable = false,
//            length = 3
//    )
//    private String currency;
//
//    @Enumerated(EnumType.STRING)
//    @Column(
//            name = "method",
//            nullable = false,
//            length = 50
//    )
//    private PaymentMethod method;
//
//    @Enumerated(EnumType.STRING)
//    @Column(
//            name = "status",
//            nullable = false,
//            length = 50
//    )
//    private PaymentStatus status;
//
//    @Column(name = "created_at",
//            nullable = false,
//            updatable = false)
//    private Instant createdAt;
//
//    @Column(name = "updated_at")
//    private Instant updatedAt;
//
//    @Column(name = "completed_at")
//    private Instant completedAt;
//}
