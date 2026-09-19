package com.shopsense.orderservice.mapper;

import com.shopsense.orderservice.entity.Order;
import com.shopsense.orderservice.entity.OrderItem;
import com.shopsense.orderservice.entity.OrderStatusHistory;
import com.shopsense.orderservice.response.OrderItemResponse;
import com.shopsense.orderservice.response.OrderResponse;
import com.shopsense.orderservice.response.OrderStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {


    OrderResponse toResponse( Order order );

    OrderItemResponse toItemResponse( OrderItem orderItem );

    OrderStatusHistoryResponse toStatusHistoryResponse( OrderStatusHistory statusHistory );
}