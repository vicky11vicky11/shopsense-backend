package com.shopsense.orderservice.response;

import com.shopsense.orderservice.enums.RefundStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class PaymentRefundResponse {
    private UUID id;

    private UUID paymentId;

    private String gatewayRefundId;

    private BigDecimal amount;

    private RefundStatus status;

    private String reason;

    private String failureReason;

    private Instant createdAt;

    private Instant updatedAt;
}
