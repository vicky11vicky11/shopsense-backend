package com.shopsense.inventoryservice.service.impl;

import com.shopsense.inventoryservice.entity.Inventory;
import com.shopsense.inventoryservice.entity.StockMovement;
import com.shopsense.inventoryservice.enums.StockMovementType;
import com.shopsense.inventoryservice.exceptions.InventoryNotFoundException;
import com.shopsense.inventoryservice.mapper.InventoryMapper;
import com.shopsense.inventoryservice.repository.InventoryRepository;
import com.shopsense.inventoryservice.repository.StockMovementRepository;
import com.shopsense.inventoryservice.request.CreateInventoryRequest;
import com.shopsense.inventoryservice.request.StockAdjustmentRequest;
import com.shopsense.inventoryservice.request.UpdateInventoryRequest;
import com.shopsense.inventoryservice.response.AvailabilityResponse;
import com.shopsense.inventoryservice.response.InventoryResponse;
import com.shopsense.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    private final StockMovementRepository stockMovementRepository;

    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryResponse create( CreateInventoryRequest request ) {
        if ( inventoryRepository.existsByProductId(request.getProductId()) ) {
            throw new IllegalArgumentException("Inventory already exists for product: " + request.getProductId());
        }
        Inventory inventory = inventoryMapper.toEntity(request);
        if ( inventory.getLowStockThreshold() == null ) {
            inventory.setLowStockThreshold(5);
        }
        Inventory saved = inventoryRepository.saveAndFlush(inventory);
        if ( saved.getQuantity() > 0 ) {
            StockMovement movement = StockMovement.builder()
                    .productId(saved.getProductId())
                    .type(StockMovementType.STOCK_IN)
                    .quantity(saved.getQuantity())
                    .reason("Initial inventory")
                    .build();
            stockMovementRepository.save(movement);
        }
        log.info("Inventory created successfully: productId={}", saved.getProductId());
        return inventoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProductId( UUID productId ) {
        Inventory inventory = getInventory(productId);
        log.info("Inventory get successfully: productId={}", inventory.getProductId());
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse update( UUID productId, UpdateInventoryRequest request ) {
        Inventory inventory = getInventory(productId);
        int currentQuantity = inventory.getQuantity();
        int newQuantity = request.getQuantity();
        int reserved = inventory.getReservedQuantity();
        if ( newQuantity < reserved ) {
            throw new IllegalArgumentException("Quantity cannot be less than reserved quantity");
        }
        inventory.setQuantity(newQuantity);
        if ( request.getLowStockThreshold() != null ) {
            inventory.setLowStockThreshold(request.getLowStockThreshold());
        }
        Inventory updated = inventoryRepository.saveAndFlush(inventory);
        int difference = newQuantity - currentQuantity;
        if ( difference != 0 ) {
            StockMovementType movementType = difference > 0 ? StockMovementType.STOCK_IN : StockMovementType.STOCK_OUT;
            StockMovement movement = StockMovement.builder()
                    .productId(productId)
                    .type(movementType)
                    .quantity(Math.abs(difference))
                    .reason("Inventory quantity updated")
                    .build();
            stockMovementRepository.save(movement);
        }
        return inventoryMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock( UUID productId, StockAdjustmentRequest request ) {
        Inventory inventory = getInventory(productId);
        int adjustment = request.getQuantity();
        int newQuantity = inventory.getQuantity() + adjustment;
        if ( newQuantity < inventory.getReservedQuantity() ) {
            throw new IllegalArgumentException("Stock adjustment cannot reduce quantity below reserved quantity");
        }
        int updatedRows = inventoryRepository.adjustStock(productId, adjustment);
        if ( updatedRows == 0 ) {
            throw new IllegalArgumentException("Stock adjustment failed for product: " + productId);
        }
        StockMovement movement = StockMovement.builder()
                .productId(productId)
                .type(StockMovementType.ADJUSTMENT)
                .quantity(Math.abs(adjustment))
                .reason(request.getReason())
                .build();
        stockMovementRepository.save(movement);
        Inventory updated = getInventory(productId);
        log.info("Stock adjusted: productId={}, adjustment={}", productId, adjustment);
        return inventoryMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability( UUID productId ) {
        Inventory inventory = getInventory(productId);
        int available = inventory.getQuantity() - inventory.getReservedQuantity();
        return AvailabilityResponse.builder()
                .productId(productId)
                .availableQuantity(available)
                .available(available > 0)
                .lowStock(available <= inventory.getLowStockThreshold())
                .build();
    }

    private Inventory getInventory( UUID productId ) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
    }
}