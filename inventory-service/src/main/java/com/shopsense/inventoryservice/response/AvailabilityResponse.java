package com.shopsense.inventoryservice.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {

    private UUID productId;

    private Integer availableQuantity;

    private boolean available;

    private boolean lowStock;
}