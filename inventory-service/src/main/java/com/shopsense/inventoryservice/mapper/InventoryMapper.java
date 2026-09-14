package com.shopsense.inventoryservice.mapper;

import com.shopsense.inventoryservice.entity.Inventory;
import com.shopsense.inventoryservice.request.CreateInventoryRequest;
import com.shopsense.inventoryservice.request.UpdateInventoryRequest;
import com.shopsense.inventoryservice.response.InventoryResponse;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservedQuantity", constant = "0")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Inventory toEntity( CreateInventoryRequest request );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "reservedQuantity", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity( UpdateInventoryRequest request, @MappingTarget Inventory inventory );

    @Mapping(target = "availableQuantity", expression = "java(inventory.getQuantity() - inventory.getReservedQuantity())")
    @Mapping(target = "lowStock", expression = "java(inventory.getQuantity() - inventory.getReservedQuantity() <= inventory.getLowStockThreshold())")
    InventoryResponse toResponse( Inventory inventory );
}