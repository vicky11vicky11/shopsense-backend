package com.shopsense.paymentservice.request;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GatewayRefundRequest {

    private String gatewayPaymentId;

    private BigDecimal amount;

    private String reason;
}