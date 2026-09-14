package com.shopsense.inventoryservice.response;

import com.shopsense.inventoryservice.enums.ReservationStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationResponse {

    private UUID orderId;

    private ReservationStatus status;

    private List<ReservationItemResponse> items;

    private Instant createdAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationItemResponse {

        private UUID reservationId;

        private UUID productId;

        private Integer quantity;
    }
}