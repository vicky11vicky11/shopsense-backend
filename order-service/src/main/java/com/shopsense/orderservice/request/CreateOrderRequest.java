package com.shopsense.orderservice.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank
    private String shippingAddressId;

    @NotBlank
    private String currency;
}