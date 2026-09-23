package com.shopsense.paymentservice.request;

import com.shopsense.paymentservice.enums.Currency;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CreateGatewayPaymentRequest {

    private UUID orderId;

    private String userId;

    private BigDecimal amount;

    private Currency currency;

    private String description;
}