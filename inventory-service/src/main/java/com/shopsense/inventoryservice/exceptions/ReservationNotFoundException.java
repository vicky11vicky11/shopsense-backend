package com.shopsense.inventoryservice.exceptions;

import java.util.UUID;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException( UUID reservationId ) {
        super("Reservation not found: " + reservationId);
    }
}