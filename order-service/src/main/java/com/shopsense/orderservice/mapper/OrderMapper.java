package com.shopsense.orderservice.mapper;

import com.shopsense.commonservice.response.OrderItemResponse;
import com.shopsense.commonservice.response.OrderStatusHistoryResponse;
import com.shopsense.orderservice.entity.Order;
import com.shopsense.orderservice.entity.OrderItem;
import com.shopsense.orderservice.entity.OrderStatusHistory;
import com.shopsense.orderservice.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {


    OrderResponse toResponse( Order order );

    OrderItemResponse toItemResponse( OrderItem orderItem );

    OrderStatusHistoryResponse toStatusHistoryResponse( OrderStatusHistory statusHistory );
}