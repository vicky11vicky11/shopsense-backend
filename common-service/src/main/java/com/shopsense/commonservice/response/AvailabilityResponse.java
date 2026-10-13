package com.shopsense.commonservice.response;

import com.shopsense.commonservice.enums.StockStatus;
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

    private StockStatus stockStatus;
}