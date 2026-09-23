package com.shopsense.paymentservice.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shopsense.paymentservice.enums.Currency;
import com.shopsense.paymentservice.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductOrderResponse {

    private UUID id;

    private String orderNumber;

    private String userId;

    private OrderStatus status;

    private BigDecimal subtotal;

    private BigDecimal totalAmount;

    private Currency currency;

}