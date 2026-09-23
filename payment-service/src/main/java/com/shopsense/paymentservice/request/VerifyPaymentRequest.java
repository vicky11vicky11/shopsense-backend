package com.shopsense.paymentservice.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentRequest {

    @NotBlank
    private String gatewayOrderId;

    @NotBlank
    private String gatewayPaymentId;

    @NotBlank
    private String signature;
}