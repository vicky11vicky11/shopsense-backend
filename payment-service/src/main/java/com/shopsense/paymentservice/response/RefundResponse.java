package com.shopsense.paymentservice.response;

import com.shopsense.paymentservice.enums.RefundStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefundResponse {

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
