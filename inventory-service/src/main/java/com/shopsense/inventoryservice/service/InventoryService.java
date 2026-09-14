package com.shopsense.inventoryservice.service;

import com.shopsense.inventoryservice.request.BulkInventoryRequest;
import com.shopsense.inventoryservice.request.CreateInventoryRequest;
import com.shopsense.inventoryservice.request.StockAdjustmentRequest;
import com.shopsense.inventoryservice.request.UpdateInventoryRequest;
import com.shopsense.inventoryservice.response.AvailabilityResponse;
import com.shopsense.inventoryservice.response.InventoryResponse;
import com.shopsense.inventoryservice.response.StockMovementResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    InventoryResponse create( CreateInventoryRequest request );

    InventoryResponse getByProductId( UUID productId );

    InventoryResponse update( UUID productId, UpdateInventoryRequest request );

    InventoryResponse adjustStock( UUID productId, StockAdjustmentRequest request );

    AvailabilityResponse getAvailability( UUID productId );

    List<AvailabilityResponse> getBulkAvailability( BulkInventoryRequest request);

    List<StockMovementResponse> getMovementHistory(UUID productId);

    List<StockMovementResponse> getMovementHistoryByReference(
            UUID referenceId
    );
}