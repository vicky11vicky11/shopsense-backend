package com.shopsense.inventoryservice.client;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange
public interface OrderServiceClient {

    @GetExchange("/exists/{orderId}")
    boolean idOrderExists( UUID orderId );
}
