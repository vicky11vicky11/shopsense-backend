package com.shopsense.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stores", uniqueConstraints = { @UniqueConstraint(name = "uk_store_seller_address", columnNames = { "seller_id", "address_id" }) })
public class Store {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String addressId;

    @Column(nullable = false)
    private String storeName;

    private String storeImageId;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

}
