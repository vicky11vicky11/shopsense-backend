package com.shopsense.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_idempotency", schema = "orders", uniqueConstraints = { @UniqueConstraint(name = "uk_order_idempotency_user_key", columnNames = { "user_id", "idempotency_key" }) }, indexes = { @Index(name = "idx_order_idempotency_order_id", columnList = "order_id") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderIdempotency {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_order_idempotency_order"))
    private Order order;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}