package com.shopsense.paymentservice.request;

import com.shopsense.commonservice.enums.Currency;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CreateGatewayPaymentRequest {

    private UUID orderId;

    private UUID userId;

    private BigDecimal amount;

    private Currency currency;

    private String description;
}