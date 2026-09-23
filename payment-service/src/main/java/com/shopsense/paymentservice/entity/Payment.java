package com.shopsense.paymentservice.entity;

import com.shopsense.paymentservice.enums.Currency;
import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = { @Index(name = "idx_payment_order_id", columnList = "order_id"), @Index(name = "idx_payment_user_id", columnList = "user_id"), @Index(name = "idx_payment_gateway_payment_id", columnList = "payment_gateway, gateway_payment_id"), @Index(name = "idx_payment_gateway_order_id", columnList = "payment_gateway, gateway_order_id") }, uniqueConstraints = { @UniqueConstraint(name = "uk_payment_gateway_payment_id", columnNames = { "payment_gateway", "gateway_payment_id" }), @UniqueConstraint(name = "uk_payment_gateway_order_id", columnNames = { "payment_gateway", "gateway_order_id" }) })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_gateway", nullable = false, length = 20)
    private PaymentGateway paymentGateway;

    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "refunded_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}