package com.shopsense.orderservice.response;

import com.shopsense.orderservice.enums.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private UUID id;

    private UUID productId;

    private String productName;

    private String productImageUrl;

    private BigDecimal unitPrice;

    private Integer quantity;

    private BigDecimal lineTotal;

    private Currency currency;
}