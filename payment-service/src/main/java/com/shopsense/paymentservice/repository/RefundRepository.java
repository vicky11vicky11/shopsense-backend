package com.shopsense.paymentservice.repository;

import com.shopsense.paymentservice.entity.Refund;
import com.shopsense.paymentservice.enums.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {
    List<Refund> findByPaymentId( UUID paymentId );

    List<Refund> findByStatus( RefundStatus status );
}
