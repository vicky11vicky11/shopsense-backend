package com.shopsense.inventoryservice.client;

import com.shopsense.inventoryservice.util.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;

import java.util.UUID;

@HttpExchange(AppUrl.ORDER_URL)
public interface OrderServiceClient {

    @PatchExchange("/{orderId}/reservation-failed")
    void reservationFailedStatusUpdate( @PathVariable UUID orderId );

}
