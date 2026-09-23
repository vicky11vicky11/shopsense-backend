package com.shopsense.paymentservice.service.impl;

import com.shopsense.paymentservice.client.OrderServiceClient;
import com.shopsense.paymentservice.entity.Payment;
import com.shopsense.paymentservice.entity.ProcessedWebhookEvent;
import com.shopsense.paymentservice.entity.Refund;
import com.shopsense.paymentservice.enums.OrderStatus;
import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.enums.PaymentStatus;
import com.shopsense.paymentservice.enums.RefundStatus;
import com.shopsense.paymentservice.exceptions.PaymentException;
import com.shopsense.paymentservice.provider.PaymentProvider;
import com.shopsense.paymentservice.provider.PaymentProviderFactory;
import com.shopsense.paymentservice.repository.PaymentRepository;
import com.shopsense.paymentservice.repository.ProcessedWebhookEventRepository;
import com.shopsense.paymentservice.repository.RefundRepository;
import com.shopsense.paymentservice.request.*;
import com.shopsense.paymentservice.response.GatewayPaymentResponse;
import com.shopsense.paymentservice.response.GatewayRefundResponse;
import com.shopsense.paymentservice.response.PaymentResponse;
import com.shopsense.paymentservice.response.ProductOrderResponse;
import com.shopsense.paymentservice.response.RefundResponse;
import com.shopsense.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final RefundRepository refundRepository;

    private final ProcessedWebhookEventRepository processedWebhookEventRepository;

    private final OrderServiceClient orderServiceClient;

    private final PaymentProviderFactory paymentProviderFactory;

    @Override
    @Transactional
    public PaymentResponse createPayment( String userId, CreatePaymentRequest request ) {
        ProductOrderResponse order = orderServiceClient.getOrderById(userId, request.getOrderId());
        validateOrderForPayment(order, request.getOrderId());
        Payment existingPayment = paymentRepository.findByOrderIdAndPaymentGateway(request.getOrderId(), request.getPaymentGateway())
                .orElse(null);
        if ( existingPayment != null ) {
            if ( existingPayment.getStatus() == PaymentStatus.SUCCESS ) {
                throw new PaymentException("Order has already been paid");
            }
            if ( existingPayment.getStatus() == PaymentStatus.PENDING || existingPayment.getStatus() == PaymentStatus.PROCESSING ) {
                return toResponse(existingPayment);
            }
        }
        PaymentProvider provider = paymentProviderFactory.getProvider(request.getPaymentGateway());
        GatewayPaymentResponse gatewayResult = provider.createPayment(CreateGatewayPaymentRequest.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .amount(order.getTotalAmount())
                .currency(order.getCurrency())
                .description("Payment for order " + request.getOrderId())
                .build());
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .paymentGateway(request.getPaymentGateway())
                .gatewayOrderId(gatewayResult.getGatewayOrderId())
                .gatewayPaymentId(gatewayResult.getGatewayPaymentId())
                .amount(order.getTotalAmount())
                .currency(order.getCurrency())
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);
        return toResponse(payment, gatewayResult);
    }

    @Override
    @Transactional
    public PaymentResponse verifyRazorpayPayment( String userId, UUID paymentId, String gatewayOrderId, String gatewayPaymentId, String signature ) {
        Payment payment = getOwnedPayment(userId, paymentId);
        if ( payment.getPaymentGateway() != PaymentGateway.RAZORPAY ) {
            throw new PaymentException("Payment does not belong to Razorpay");
        }
        PaymentProvider provider = paymentProviderFactory.getProvider(PaymentGateway.RAZORPAY);
        boolean verified = provider.verifyPayment(VerifyPaymentRequest.builder()
                .gatewayOrderId(gatewayOrderId)
                .gatewayPaymentId(gatewayPaymentId)
                .signature(signature)
                .build());
        if ( !verified ) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment signature verification failed");
            paymentRepository.save(payment);
            updateOrderStatus(userId, payment, OrderStatus.PAYMENT_FAILED, "Payment verification failed", "Payment verification failed by payment gateway");
            throw new PaymentException("Invalid payment signature");
        }
        payment.setGatewayOrderId(gatewayOrderId);
        payment.setGatewayPaymentId(gatewayPaymentId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment = paymentRepository.save(payment);
        updateOrderStatus(userId, payment, OrderStatus.CONFIRMED, "Payment successful", "Payment successfully verified by payment gateway");
        return toResponse(payment);
    }

    private void updateOrderStatus( String userId, Payment payment, OrderStatus status, String reason, String description ) {
        orderServiceClient.updateOrderStatus(userId, payment.getOrderId(), UpdateOrderStatusRequest.builder()
                .status(status)
                .reason(reason)
                .description(description)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment( String userId, UUID paymentId ) {
        return toResponse(getOwnedPayment(userId, paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId( String userId, UUID orderId ) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment not found"));
        if ( !payment.getUserId()
                .equals(userId) ) {
            throw new PaymentException("You are not allowed to access this payment");
        }
        return toResponse(payment);
    }

    @Override
    @Transactional
    public RefundResponse refundPayment( String userId, UUID paymentId, BigDecimal amount, String reason ) {
        Payment payment = getOwnedPayment(userId, paymentId);
        if ( payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED ) {
            throw new PaymentException("Payment cannot be refunded");
        }
        BigDecimal availableAmount = payment.getAmount()
                .subtract(payment.getRefundedAmount());
        if ( amount.compareTo(availableAmount) > 0 ) {
            throw new PaymentException("Refund amount exceeds refundable amount");
        }
        PaymentProvider provider = paymentProviderFactory.getProvider(payment.getPaymentGateway());
        Refund refund = Refund.builder()
                .payment(payment)
                .amount(amount)
                .reason(reason)
                .status(RefundStatus.PENDING)
                .build();
        refund = refundRepository.save(refund);
        try {
            GatewayRefundResponse result = provider.refund(GatewayRefundRequest.builder()
                    .gatewayPaymentId(payment.getGatewayPaymentId())
                    .amount(amount)
                    .reason(reason)
                    .build());
            refund.setGatewayRefundId(result.getGatewayRefundId());
            refund.setStatus(RefundStatus.REFUNDED);
            payment.setRefundedAmount(payment.getRefundedAmount()
                    .add(amount));
            if ( payment.getRefundedAmount()
                    .compareTo(payment.getAmount()) == 0 ) {
                payment.setStatus(PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }
            refund = refundRepository.save(refund);
            paymentRepository.save(payment);
            return toRefundResponse(refund);
        } catch ( Exception exception ) {
            refund.setStatus(RefundStatus.FAILED);
            refund.setFailureReason(exception.getMessage());
            refundRepository.save(refund);
            throw exception;
        }
    }

    @Override
    @Transactional
    public void handleWebhook( PaymentGateway gateway, String payload, String signature, String eventId ) {
        log.info("WEBHOOK PROCESSING STARTED | gateway={} | eventId={}", gateway, eventId);
        PaymentProvider provider = paymentProviderFactory.getProvider(gateway);
        log.debug("WEBHOOK PROVIDER RESOLVED | gateway={} | provider={}", gateway, provider.getClass()
                .getSimpleName());
        PaymentWebhookEvent event = provider.parseWebhook(payload, signature, eventId);
        log.info("WEBHOOK PARSED | gateway={} | eventId={} | eventType={} | paymentId={} | orderId={} | status={}", gateway, event.getEventId(), event.getEventType(), event.getGatewayPaymentId(), event.getGatewayOrderId(), event.getPaymentStatus());
        if ( event.getEventId() == null ) {
            log.error("WEBHOOK REJECTED | gateway={} | reason=Missing event ID", gateway);
            throw new PaymentException("Webhook event ID is missing");
        }
        boolean alreadyProcessed = processedWebhookEventRepository.existsByGatewayAndEventId(gateway.name(), event.getEventId());
        if ( alreadyProcessed ) {
            log.warn("WEBHOOK ALREADY PROCESSED | gateway={} | eventId={} | eventType={}", gateway, event.getEventId(), event.getEventType());
            return;
        }
        if ( event.getPaymentStatus() == null ) {
            log.info("WEBHOOK IGNORED | gateway={} | eventId={} | eventType={} | reason=Unsupported event", gateway, event.getEventId(), event.getEventType());
            saveProcessedWebhookEvent(event);
            return;
        }
        Payment payment = paymentRepository.findByPaymentGatewayAndGatewayPaymentId(gateway, event.getGatewayPaymentId())
                .orElse(null);
        if ( payment == null && event.getGatewayOrderId() != null ) {
            log.debug("PAYMENT NOT FOUND BY GATEWAY PAYMENT ID | gateway={} | paymentId={} | trying gatewayOrderId={}", gateway, event.getGatewayPaymentId(), event.getGatewayOrderId());
            payment = paymentRepository.findByPaymentGatewayAndGatewayOrderId(gateway, event.getGatewayOrderId())
                    .orElse(null);
        }
        if ( payment == null ) {
            log.error("PAYMENT NOT FOUND FOR WEBHOOK | gateway={} | eventId={} | gatewayPaymentId={} | gatewayOrderId={}", gateway, event.getEventId(), event.getGatewayPaymentId(), event.getGatewayOrderId());
            throw new PaymentException("Payment not found for webhook event: " + event.getEventId());
        }
        log.info("PAYMENT FOUND FOR WEBHOOK | paymentId={} | orderId={} | currentStatus={} | webhookStatus={}", payment.getId(), payment.getOrderId(), payment.getStatus(), event.getPaymentStatus());
        if ( event.getPaymentStatus() == PaymentStatus.SUCCESS ) {
            log.info("PROCESSING SUCCESSFUL PAYMENT | paymentId={} | orderId={}", payment.getId(), payment.getOrderId());
            handleSuccessfulPayment(payment, event);
        } else if ( event.getPaymentStatus() == PaymentStatus.FAILED ) {
            log.warn("PROCESSING FAILED PAYMENT | paymentId={} | orderId={} | reason={}", payment.getId(), payment.getOrderId(), event.getFailureReason());
            handleFailedPayment(payment, event);
        }
        saveProcessedWebhookEvent(event);
        log.info("WEBHOOK PROCESSING COMPLETED | gateway={} | eventId={} | eventType={}", gateway, event.getEventId(), event.getEventType());
    }

    private Payment getOwnedPayment( String userId, UUID paymentId ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));
        if ( !payment.getUserId()
                .equals(userId) ) {
            throw new PaymentException("You are not allowed to access this payment");
        }
        return payment;
    }

    private void validateOrderForPayment( ProductOrderResponse order, UUID orderId ) {
        if ( order == null ) {
            throw new PaymentException("Order not found: " + orderId);
        }
        if ( order.getStatus() != OrderStatus.PAYMENT_PENDING ) {
            throw new PaymentException("Order is not ready for payment. Current status: " + order.getStatus());
        }
        if ( order.getTotalAmount() == null || order.getTotalAmount()
                .compareTo(BigDecimal.ZERO) <= 0 ) {
            throw new PaymentException("Order total amount must be greater than zero");
        }
        if ( order.getCurrency() == null ) {
            throw new PaymentException("Order currency is required");
        }
    }

    private PaymentResponse toResponse( Payment payment ) {
        return toResponse(payment, null);
    }

    private PaymentResponse toResponse( Payment payment, GatewayPaymentResponse gatewayPaymentResponse ) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .paymentGateway(payment.getPaymentGateway())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .refundedAmount(payment.getRefundedAmount())
                .failureReason(payment.getFailureReason())
                .checkoutKey(gatewayPaymentResponse != null ? gatewayPaymentResponse.getCheckoutKey() : null)
                .checkoutClientSecret(gatewayPaymentResponse != null ? gatewayPaymentResponse.getCheckoutClientSecret() : null)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private RefundResponse toRefundResponse( Refund refund ) {
        return RefundResponse.builder()
                .id(refund.getId())
                .paymentId(refund.getPayment()
                        .getId())
                .gatewayRefundId(refund.getGatewayRefundId())
                .amount(refund.getAmount())
                .status(refund.getStatus())
                .reason(refund.getReason())
                .failureReason(refund.getFailureReason())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }

    private void handleSuccessfulPayment( Payment payment, PaymentWebhookEvent event ) {
        if ( payment.getStatus() == PaymentStatus.SUCCESS ) {
            log.info("PAYMENT ALREADY SUCCESSFUL | paymentId={} | orderId={}", payment.getId(), payment.getOrderId());
            return;
        }
        log.info("SETTING PAYMENT SUCCESS | paymentId={} | orderId={} | gatewayPaymentId={}", payment.getId(), payment.getOrderId(), event.getGatewayPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        if ( event.getGatewayPaymentId() != null ) {
            payment.setGatewayPaymentId(event.getGatewayPaymentId());
        }
        if ( event.getGatewayOrderId() != null ) {
            payment.setGatewayOrderId(event.getGatewayOrderId());
        }
        if ( event.getPaymentMethod() != null ) {
            payment.setPaymentMethod(event.getPaymentMethod());
        }
        payment.setFailureReason(null);
        paymentRepository.save(payment);
        log.info("UPDATING ORDER TO CONFIRMED | orderId={}", payment.getOrderId());
        updateOrderStatus(payment.getUserId(), payment, OrderStatus.CONFIRMED, "Payment successful", "Payment successfully confirmed by payment gateway");
        log.info("ORDER CONFIRMED AFTER PAYMENT | orderId={}", payment.getOrderId());
    }

    private void handleFailedPayment( Payment payment, PaymentWebhookEvent event ) {
        if ( payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.REFUNDED || payment.getStatus() == PaymentStatus.PARTIALLY_REFUNDED ) {
            log.warn("IGNORING FAILED WEBHOOK | paymentId={} | currentStatus={} | webhookEvent={}", payment.getId(), payment.getStatus(), event.getEventType());
            return;
        }
        String failureReason = event.getFailureReason() != null ? event.getFailureReason() : "Payment failed";
        log.warn("SETTING PAYMENT FAILED | paymentId={} | orderId={} | reason={}", payment.getId(), payment.getOrderId(), failureReason);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);
        paymentRepository.save(payment);
        log.warn("PAYMENT MARKED FAILED | paymentId={} | orderId={}", payment.getId(), payment.getOrderId());
        updateOrderStatus(payment.getUserId(), payment, OrderStatus.PAYMENT_FAILED, "Payment failed", failureReason);
        log.warn("ORDER MARKED PAYMENT_FAILED | orderId={}", payment.getOrderId());
    }

    private void saveProcessedWebhookEvent( PaymentWebhookEvent event ) {
        ProcessedWebhookEvent processedEvent = ProcessedWebhookEvent.builder()
                .gateway(event.getGateway()
                        .name())
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .processedAt(java.time.Instant.now())
                .build();
        processedWebhookEventRepository.save(processedEvent);
    }
}