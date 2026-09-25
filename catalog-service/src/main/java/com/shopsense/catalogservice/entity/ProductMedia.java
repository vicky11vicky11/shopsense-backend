package com.shopsense.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
    @Entity
@Table(name = "product_media", uniqueConstraints = { @UniqueConstraint(name = "uk_product_media", columnNames = { "product_id", "media_id" }), @UniqueConstraint(name = "uk_product_media_display_order", columnNames = { "product_id", "display_order" }) })
public class ProductMedia {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private UUID mediaId;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean primaryImage = false;
}