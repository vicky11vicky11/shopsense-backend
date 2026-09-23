package com.shopsense.paymentservice.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayRefundResponse {

    private String gatewayRefundId;

    private String status;

    private String failureReason;
}