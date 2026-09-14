package com.shopsense.inventoryservice.scheduler;

import com.shopsense.inventoryservice.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationScheduler {

    private final StockReservationService reservationService;

    @Scheduled(fixedDelay = 60_000)
    public void expireReservations() {
        log.debug("Starting expired reservation cleanup");
        reservationService.expireReservations();
        log.debug("Expired reservation cleanup completed");
    }
}