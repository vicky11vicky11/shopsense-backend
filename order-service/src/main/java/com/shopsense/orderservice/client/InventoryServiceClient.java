package com.shopsense.orderservice.client;

import com.shopsense.commonservice.response.AvailabilityResponse;
import com.shopsense.commonservice.util.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange(AppUrl.INVENTORY_URL)
public interface InventoryServiceClient {

    @GetExchange("/{productId}/availability")
    AvailabilityResponse getAvailability( @PathVariable UUID productId );

}
