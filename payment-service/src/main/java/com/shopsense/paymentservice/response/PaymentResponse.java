package com.shopsense.paymentservice.response;

import com.shopsense.paymentservice.enums.Currency;
import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private UUID id;

    private UUID orderId;

    private String userId;

    private PaymentGateway paymentGateway;

    private String gatewayOrderId;

    private String gatewayPaymentId;

    private BigDecimal amount;

    private Currency currency;

    private PaymentStatus status;

    private String paymentMethod;

    private BigDecimal refundedAmount;

    private String failureReason;

    private String checkoutKey;

    private String checkoutClientSecret;

    private Instant createdAt;

    private Instant updatedAt;

}
