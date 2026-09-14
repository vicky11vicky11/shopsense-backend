package com.shopsense.inventoryservice.response;

import com.shopsense.inventoryservice.enums.StockMovementType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {

    private UUID id;

    private UUID productId;

    private StockMovementType type;

    private Integer quantity;

    private UUID referenceId;

    private String reason;

    private Instant createdAt;
}