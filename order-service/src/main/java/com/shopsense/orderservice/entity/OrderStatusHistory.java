package com.shopsense.orderservice.entity;

import com.shopsense.orderservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_status_history", schema = "orders", indexes = { @Index(name = "idx_order_status_history_order_id", columnList = "order_id"), @Index(name = "idx_order_status_history_created_at", columnList = "created_at") })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_status_history_order"))
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private OrderStatus toStatus;

    @Column(length = 100)
    private String reason;

    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

}