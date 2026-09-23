package com.shopsense.paymentservice.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayPaymentResponse {

    private String gatewayOrderId;

    private String gatewayPaymentId;

    private String checkoutKey;

    private String checkoutClientSecret;

    private String paymentMethod;

    private String status;

    private String failureReason;
}