package com.shopsense.paymentservice.repository;

import com.shopsense.paymentservice.entity.Payment;
import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentGatewayAndGatewayPaymentId( PaymentGateway paymentGateway, String gatewayPaymentId );

    Optional<Payment> findByPaymentGatewayAndGatewayOrderId( PaymentGateway paymentGateway, String gatewayOrderId );

    Optional<Payment> findByOrderId( UUID orderId );

    Optional<Payment> findByOrderIdAndPaymentGateway( UUID orderId, PaymentGateway paymentGateway );

    List<Payment> findByUserId( String userId );

    List<Payment> findByStatus( PaymentStatus status );
}