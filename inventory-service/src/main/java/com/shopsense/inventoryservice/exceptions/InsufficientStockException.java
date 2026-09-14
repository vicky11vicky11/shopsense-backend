package com.shopsense.inventoryservice.exceptions;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    private final UUID productId;

    public InsufficientStockException( UUID productId ) {
        super("Insufficient stock for product: " + productId);
        this.productId = productId;
    }

    public UUID getProductId() {
        return productId;
    }
}