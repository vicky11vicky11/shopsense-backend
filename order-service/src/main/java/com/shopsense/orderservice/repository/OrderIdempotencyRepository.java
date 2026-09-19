package com.shopsense.orderservice.repository;

import com.shopsense.orderservice.entity.OrderIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderIdempotencyRepository extends JpaRepository<OrderIdempotency, UUID> {

    Optional<OrderIdempotency> findByUserIdAndIdempotencyKey( String userId, String idempotencyKey );
}