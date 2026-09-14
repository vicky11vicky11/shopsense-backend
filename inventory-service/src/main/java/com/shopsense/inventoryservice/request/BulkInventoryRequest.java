package com.shopsense.inventoryservice.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkInventoryRequest {

    @NotEmpty(message = "Product IDs are required")
    @Size(max = 100, message = "A maximum of 100 product IDs is allowed")
    private List<UUID> productIds;

}