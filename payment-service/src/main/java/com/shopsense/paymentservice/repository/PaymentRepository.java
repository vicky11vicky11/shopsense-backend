package com.shopsense.paymentservice.repository;

import com.shopsense.paymentservice.entity.Payment;
import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentGatewayAndGatewayPaymentId( PaymentGateway paymentGateway, String gatewayPaymentId );

    Optional<Payment> findByPaymentGatewayAndGatewayOrderId( PaymentGateway paymentGateway, String gatewayOrderId );

    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc( UUID orderId );

    Optional<Payment> findFirstByOrderIdAndPaymentGatewayOrderByCreatedAtDesc( UUID orderId, PaymentGateway paymentGateway );

    boolean existsByOrderIdAndStatusAndIdNot( UUID orderId, PaymentStatus status, UUID paymentId );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.orderId = :orderId order by p.createdAt")
    List<Payment> lockAllByOrderId(@Param("orderId") UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :paymentId")
    Optional<Payment> lockById(@Param("paymentId") UUID paymentId);

    List<Payment> findByUserId( String userId );

    List<Payment> findByStatus( PaymentStatus status );
}
