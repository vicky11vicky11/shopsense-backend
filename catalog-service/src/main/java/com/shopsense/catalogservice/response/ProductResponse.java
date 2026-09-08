package com.shopsense.catalogservice.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;

    private UUID storeId;

    private UUID categoryId;

    private UUID brandId;

    private String name;

    private String description;

    private BigDecimal price;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;
}