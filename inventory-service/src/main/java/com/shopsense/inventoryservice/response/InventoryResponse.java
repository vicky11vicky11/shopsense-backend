package com.shopsense.inventoryservice.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private UUID id;

    private UUID productId;

    private Integer quantity;

    private Integer reservedQuantity;

    private Integer availableQuantity;

    private Integer lowStockThreshold;

    private boolean lowStock;

    private Instant createdAt;

    private Instant updatedAt;
}