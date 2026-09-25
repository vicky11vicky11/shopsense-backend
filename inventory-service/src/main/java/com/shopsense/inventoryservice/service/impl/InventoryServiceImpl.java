package com.shopsense.inventoryservice.service.impl;

import com.shopsense.inventoryservice.client.ProductServiceClient;
import com.shopsense.inventoryservice.entity.Inventory;
import com.shopsense.inventoryservice.entity.StockMovement;
import com.shopsense.inventoryservice.enums.StockMovementType;
import com.shopsense.inventoryservice.enums.StockStatus;
import com.shopsense.inventoryservice.exception.InventoryNotFoundException;
import com.shopsense.inventoryservice.exception.ResourceNotFoundException;
import com.shopsense.inventoryservice.mapper.InventoryMapper;
import com.shopsense.inventoryservice.repository.InventoryRepository;
import com.shopsense.inventoryservice.repository.StockMovementRepository;
import com.shopsense.inventoryservice.request.BulkInventoryRequest;
import com.shopsense.inventoryservice.request.CreateInventoryRequest;
import com.shopsense.inventoryservice.request.StockAdjustmentRequest;
import com.shopsense.inventoryservice.request.UpdateInventoryRequest;
import com.shopsense.inventoryservice.response.AvailabilityResponse;
import com.shopsense.inventoryservice.response.InventoryResponse;
import com.shopsense.inventoryservice.response.StockMovementResponse;
import com.shopsense.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    @Value("${inventory.default-low-stock-threshold}")
    private Integer DEFAULT_LOW_STOCK_THRESHOLD;

    private final InventoryRepository inventoryRepository;

    private final StockMovementRepository stockMovementRepository;

    private final InventoryMapper inventoryMapper;

    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional
    public InventoryResponse create( CreateInventoryRequest request ) {
        validateProduct(request.getProductId());
        if ( inventoryRepository.existsByProductId(request.getProductId()) ) {
            throw new IllegalArgumentException("Inventory already exists for product: " + request.getProductId());
        }
        Inventory inventory = inventoryMapper.toEntity(request);
        if ( inventory.getLowStockThreshold() == null ) {
            inventory.setLowStockThreshold(DEFAULT_LOW_STOCK_THRESHOLD);
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
        validateProduct(productId);
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
        validateProduct(productId);
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
        return buildAvailabilityResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getBulkAvailability( BulkInventoryRequest request ) {
        List<UUID> productIds = request.getProductIds();
        if ( productIds.size() != new HashSet<>(productIds).size() ) {
            throw new IllegalArgumentException("Duplicate product IDs are not allowed");
        }
        List<Inventory> inventories = inventoryRepository.findByProductIdIn(productIds);
        Map<UUID, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getProductId, inventory -> inventory));
        return productIds.stream()
                .map(productId -> {
                    Inventory inventory = inventoryMap.get(productId);
                    if ( inventory == null ) {
                        throw new InventoryNotFoundException(productId);
                    }
                    return buildAvailabilityResponse(inventory);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementHistory( UUID productId ) {
        if ( !inventoryRepository.existsByProductId(productId) ) {
            throw new InventoryNotFoundException(productId);
        }
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toMovementResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementHistoryByReference( UUID referenceId ) {
        return stockMovementRepository.findByReferenceId(referenceId)
                .stream()
                .map(this::toMovementResponse)
                .toList();
    }

    private StockMovementResponse toMovementResponse( StockMovement movement ) {
        return StockMovementResponse.builder()
                .id(movement.getId())
                .productId(movement.getProductId())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .referenceId(movement.getReferenceId())
                .reason(movement.getReason())
                .createdAt(movement.getCreatedAt())
                .build();
    }

    private Inventory getInventory( UUID productId ) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException(productId));
    }

    private AvailabilityResponse buildAvailabilityResponse( Inventory inventory ) {
        int available = inventory.getQuantity() - inventory.getReservedQuantity();
        StockStatus status;
        if ( available <= 0 ) {
            status = StockStatus.SOLD_OUT;
        } else if ( available <= inventory.getLowStockThreshold() ) {
            status = StockStatus.LOW_STOCK;
        } else {
            status = StockStatus.IN_STOCK;
        }
        return AvailabilityResponse.builder()
                .productId(inventory.getProductId())
                .availableQuantity(available)
                .stockStatus(status)
                .build();
    }

    private void validateProduct( UUID productId ) {
        boolean productExists = productServiceClient.isProductExists(productId);
        if ( !productExists ) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
    }
}