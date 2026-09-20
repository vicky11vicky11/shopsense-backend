package com.shopsense.orderservice.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank
    private String shippingAddressId;

    @NotEmpty(message = "At least one product is required")
    private List<@Valid OrderItemRequest> items;

    @NotBlank
    private String currency;
}