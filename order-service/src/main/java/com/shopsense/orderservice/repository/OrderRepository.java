package com.shopsense.orderservice.repository;

import com.shopsense.orderservice.entity.Order;
import com.shopsense.orderservice.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndUserId(UUID id, String userId);

    Page<Order> findByUserId(String userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(
            String userId,
            OrderStatus status,
            Pageable pageable
    );

    Optional<Order> findByOrderNumber(String orderNumber);
}