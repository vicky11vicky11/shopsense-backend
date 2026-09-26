package com.shopsense.orderservice.request;

import com.shopsense.orderservice.enums.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull
    private UUID shippingAddressId;

    @NotEmpty(message = "At least one product is required")
    private List<@Valid OrderItemRequest> items;

    @NotNull
    private Currency currency;
}