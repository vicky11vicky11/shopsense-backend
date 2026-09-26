package com.shopsense.orderservice.client;

import com.shopsense.orderservice.request.ReserveStockRequest;
import com.shopsense.orderservice.response.StockReservationResponse;
import com.shopsense.orderservice.util.AppUrl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.UUID;

@HttpExchange(AppUrl.RESERVATION_URL)
public interface StockReservationServiceClient {
    @PostExchange
    StockReservationResponse reserve( @RequestBody ReserveStockRequest request );

    @GetExchange("/{reservationId}")
    StockReservationResponse getById( @PathVariable UUID reservationId );

    @GetExchange("/order/{orderId}")
    StockReservationResponse getByOrderId( @PathVariable UUID orderId );

    @PostExchange("/{reservationId}/release")
    void release( @PathVariable UUID reservationId );

    @PostExchange("/{reservationId}/consume")
    void consume( @PathVariable UUID reservationId );

    @PostExchange("/order/{orderId}/consume")
    void consumeByOrderId( @PathVariable UUID orderId );

    @PostExchange("/order/{orderId}/release")
    void releaseByOrderId( @PathVariable UUID orderId );

    @PostExchange("/order/{orderId}/restore")
    void restoreConsumedByOrderId( @PathVariable UUID orderId );

}
