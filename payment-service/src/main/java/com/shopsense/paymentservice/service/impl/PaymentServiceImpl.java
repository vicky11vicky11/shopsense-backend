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
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
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
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public PaymentResponse createPayment( String userId, CreatePaymentRequest request ) {
        log.info("CREATE PAYMENT | userId={} | orderId={} | gateway={}", userId, request.getOrderId(), request.getPaymentGateway());
        ProductOrderResponse order = orderServiceClient.getOrderById(userId, request.getOrderId());
        log.debug("ORDER FETCHED FOR PAYMENT | orderId={} | status={} | amount={} | currency={}", request.getOrderId(), order != null ? order.getStatus() : null, order != null ? order.getTotalAmount() : null, order != null ? order.getCurrency() : null);
        validateOrderForPayment(order, request.getOrderId());
        Payment existingPayment = paymentRepository.findFirstByOrderIdAndPaymentGatewayOrderByCreatedAtDesc(request.getOrderId(), request.getPaymentGateway())
                .orElse(null);
        if ( existingPayment != null ) {
            log.info("EXISTING PAYMENT FOUND | paymentId={} | orderId={} | status={} | gateway={}", existingPayment.getId(), existingPayment.getOrderId(), existingPayment.getStatus(), existingPayment.getPaymentGateway());
            if ( existingPayment.getStatus() == PaymentStatus.SUCCESS ) {
                log.warn("PAYMENT ALREADY SUCCESSFUL | paymentId={} | orderId={}", existingPayment.getId(), existingPayment.getOrderId());
                throw new PaymentException("Order has already been paid");
            }
            if ( existingPayment.getStatus() == PaymentStatus.PENDING || existingPayment.getStatus() == PaymentStatus.PROCESSING ) {
                log.info("RETURNING EXISTING ACTIVE PAYMENT | paymentId={} | orderId={} | status={}", existingPayment.getId(), existingPayment.getOrderId(), existingPayment.getStatus());
                return toResponse(existingPayment);
            }
        }
        PaymentProvider provider = paymentProviderFactory.getProvider(request.getPaymentGateway());
        log.info("CREATING GATEWAY PAYMENT | orderId={} | gateway={} | amount={} | currency={}", request.getOrderId(), request.getPaymentGateway(), order.getTotalAmount(), order.getCurrency());
        GatewayPaymentResponse gatewayResult = provider.createPayment(CreateGatewayPaymentRequest.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .amount(order.getTotalAmount())
                .currency(order.getCurrency())
                .description("Payment for order " + request.getOrderId())
                .build());
        log.info("GATEWAY PAYMENT CREATED | orderId={} | gateway={} | gatewayOrderId={} | gatewayPaymentId={}", request.getOrderId(), request.getPaymentGateway(), gatewayResult.getGatewayOrderId(), gatewayResult.getGatewayPaymentId());
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
        log.info("PAYMENT CREATED | paymentId={} | orderId={} | gateway={} | status={}", payment.getId(), payment.getOrderId(), payment.getPaymentGateway(), payment.getStatus());
        return toResponse(payment, gatewayResult);
    }

    @Override
    @Transactional(noRollbackFor = PaymentException.class)
    public PaymentResponse verifyRazorpayPayment( String userId, UUID paymentId, String gatewayOrderId, String gatewayPaymentId, String signature ) {
        log.info("VERIFY RAZORPAY PAYMENT | userId={} | paymentId={} | gatewayOrderId={} | gatewayPaymentId={}", userId, paymentId, gatewayOrderId, gatewayPaymentId);
        Payment payment = getOwnedPayment(userId, paymentId);
        if ( payment.getPaymentGateway() != PaymentGateway.RAZORPAY ) {
            log.warn("INVALID PAYMENT GATEWAY FOR RAZORPAY VERIFICATION | paymentId={} | actualGateway={}", paymentId, payment.getPaymentGateway());
            throw new PaymentException("Payment does not belong to Razorpay");
        }
        if ( payment.getGatewayOrderId() != null && !payment.getGatewayOrderId()
                .equals(gatewayOrderId) ) {
            log.warn("RAZORPAY ORDER ID MISMATCH | paymentId={} | storedOrderId={} | receivedOrderId={}", paymentId, payment.getGatewayOrderId(), gatewayOrderId);
            throw new PaymentException("Gateway order ID does not match payment");
        }
        PaymentProvider provider = paymentProviderFactory.getProvider(PaymentGateway.RAZORPAY);
        boolean verified = provider.verifyPayment(VerifyPaymentRequest.builder()
                .gatewayOrderId(gatewayOrderId)
                .gatewayPaymentId(gatewayPaymentId)
                .signature(signature)
                .build());
        if ( !verified ) {
            log.warn("RAZORPAY SIGNATURE VERIFICATION FAILED | paymentId={} | gatewayOrderId={} | gatewayPaymentId={}", paymentId, gatewayOrderId, gatewayPaymentId);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Payment signature verification failed");
            paymentRepository.save(payment);
            throw new PaymentException("Invalid payment signature");
        }
        log.info("RAZORPAY SIGNATURE VERIFIED | paymentId={} | gatewayPaymentId={}", paymentId, gatewayPaymentId);
        handleSuccessfulPayment(payment, PaymentWebhookEvent.builder()
                .gateway(PaymentGateway.RAZORPAY)
                .gatewayOrderId(gatewayOrderId)
                .gatewayPaymentId(gatewayPaymentId)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build());
        payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));
        log.info("RAZORPAY PAYMENT VERIFICATION COMPLETED | paymentId={} | status={}", paymentId, payment.getStatus());
        return toResponse(payment);
    }

    private void updateOrderStatus( String userId, Payment payment, OrderStatus status, String reason, String description ) {
        log.info("UPDATING ORDER STATUS | orderId={} | paymentId={} | newStatus={} | reason={}", payment.getOrderId(), payment.getId(), status, reason);
        ProductOrderResponse productOrderResponse = orderServiceClient.updateOrderStatus(userId, payment.getOrderId(), UpdateOrderStatusRequest.builder()
                .status(status)
                .reason(reason)
                .description(description)
                .build());
        log.info("ORDER STATUS UPDATED | orderId={} | paymentId={} | status={}", productOrderResponse.getId(), payment.getId(), status);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment( String userId, UUID paymentId ) {
        log.debug("GET PAYMENT | userId={} | paymentId={}", userId, paymentId);
        return toResponse(getOwnedPayment(userId, paymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId( String userId, UUID orderId ) {
        log.debug("GET PAYMENT BY ORDER | userId={} | orderId={}", userId, orderId);
        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new PaymentException("Payment not found"));
        if ( !payment.getUserId()
                .equals(userId) ) {
            log.warn("PAYMENT ACCESS DENIED | userId={} | paymentUserId={} | orderId={} | paymentId={}", userId, payment.getUserId(), orderId, payment.getId());
            throw new PaymentException("You are not allowed to access this payment");
        }
        return toResponse(payment);
    }

    @Override
    @Transactional(noRollbackFor = RuntimeException.class)
    public RefundResponse refundPayment( String userId, UUID paymentId, BigDecimal amount, String reason ) {
        log.info("REFUND PAYMENT | userId={} | paymentId={} | amount={} | reason={}", userId, paymentId, amount, reason);
        Payment payment = paymentRepository.lockById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));
        if ( !payment.getUserId()
                .equals(userId) ) {
            log.warn("REFUND ACCESS DENIED | userId={} | paymentUserId={} | paymentId={}", userId, payment.getUserId(), paymentId);
            throw new PaymentException("You are not allowed to access this payment");
        }
        if ( payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED ) {
            log.warn("REFUND REJECTED | paymentId={} | currentStatus={}", paymentId, payment.getStatus());
            throw new PaymentException("Payment cannot be refunded");
        }
        BigDecimal availableAmount = payment.getAmount()
                .subtract(payment.getRefundedAmount());
        if ( amount.compareTo(availableAmount) > 0 ) {
            log.warn("REFUND AMOUNT EXCEEDED | paymentId={} | requested={} | available={}", paymentId, amount, availableAmount);
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
        log.info("REFUND CREATED | refundId={} | paymentId={} | amount={} | status={}", refund.getId(), paymentId, amount, refund.getStatus());
        try {
            log.info("PROCESSING GATEWAY REFUND | refundId={} | paymentId={} | gateway={}", refund.getId(), paymentId, payment.getPaymentGateway());
            GatewayRefundResponse result = provider.refund(GatewayRefundRequest.builder()
                    .gatewayPaymentId(payment.getGatewayPaymentId())
                    .amount(amount)
                    .reason(reason)
                    .build());
            refund.setGatewayRefundId(result.getGatewayRefundId());
            boolean completed = result.getStatus() != null && ( result.getStatus()
                    .equalsIgnoreCase("succeeded") || result.getStatus()
                    .equalsIgnoreCase("processed") || result.getStatus()
                    .equalsIgnoreCase("refunded") );
            refund.setStatus(completed ? RefundStatus.REFUNDED : RefundStatus.PENDING);
            log.info("GATEWAY REFUND RESPONSE | refundId={} | gatewayRefundId={} | gatewayStatus={} | completed={}", refund.getId(), result.getGatewayRefundId(), result.getStatus(), completed);
            if ( completed ) {
                payment.setRefundedAmount(payment.getRefundedAmount()
                        .add(amount));
                if ( payment.getRefundedAmount()
                        .compareTo(payment.getAmount()) >= 0 ) {
                    payment.setStatus(PaymentStatus.REFUNDED);
                } else {
                    payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
                }
                log.info("PAYMENT REFUND STATUS UPDATED | paymentId={} | refundedAmount={} | status={}", payment.getId(), payment.getRefundedAmount(), payment.getStatus());
            }
            refund = refundRepository.save(refund);
            if ( completed ) paymentRepository.save(payment);
            log.info("REFUND PROCESSING COMPLETED | refundId={} | paymentId={} | status={}", refund.getId(), paymentId, refund.getStatus());
            return toRefundResponse(refund);
        } catch ( Exception exception ) {
            log.error("REFUND FAILED | refundId={} | paymentId={} | reason={}", refund.getId(), paymentId, exception.getMessage(), exception);
            refund.setStatus(RefundStatus.FAILED);
            refund.setFailureReason(exception.getMessage());
            refundRepository.save(refund);
            return toRefundResponse(refund);
        }
    }

    @Override
    @Transactional
    public RefundResponse refundOrder( String userId, UUID orderId ) {
        log.info("REFUND ORDER | userId={} | orderId={}", userId, orderId);
        List<Payment> capturedPayments = paymentRepository.findByUserId(userId)
                .stream()
                .filter(candidate -> candidate.getOrderId()
                        .equals(orderId))
                .filter(candidate -> candidate.getStatus() == PaymentStatus.SUCCESS || candidate.getStatus() == PaymentStatus.PARTIALLY_REFUNDED || candidate.getStatus() == PaymentStatus.REFUNDED)
                .sorted(Comparator.comparing(Payment::getCreatedAt))
                .toList();
        log.info("CAPTURED PAYMENTS FOUND FOR ORDER | orderId={} | count={}", orderId, capturedPayments.size());
        if ( capturedPayments.isEmpty() ) {
            log.warn("NO SUCCESSFUL PAYMENT FOUND FOR REFUND | orderId={} | userId={}", orderId, userId);
            throw new PaymentException("No successful payment exists for this order");
        }
        RefundResponse latestResponse = null;
        boolean allRefunded = true;
        for ( Payment payment : capturedPayments ) {
            BigDecimal amount = payment.getAmount()
                    .subtract(payment.getRefundedAmount());
            log.debug("PROCESSING ORDER REFUND PAYMENT | orderId={} | paymentId={} | refundableAmount={}", orderId, payment.getId(), amount);
            if ( amount.signum() <= 0 ) {
                latestResponse = refundRepository.findByPaymentId(payment.getId())
                        .stream()
                        .max(java.util.Comparator.comparing(Refund::getCreatedAt))
                        .map(this::toRefundResponse)
                        .orElse(latestResponse);
                continue;
            }
            if ( refundRepository.existsByPaymentIdAndStatus(payment.getId(), RefundStatus.PENDING) ) {
                log.info("PENDING REFUND ALREADY EXISTS | paymentId={} | orderId={}", payment.getId(), orderId);
                allRefunded = false;
                latestResponse = refundRepository.findByPaymentId(payment.getId())
                        .stream()
                        .filter(existing -> existing.getStatus() == RefundStatus.PENDING)
                        .max(java.util.Comparator.comparing(Refund::getCreatedAt))
                        .map(this::toRefundResponse)
                        .orElse(latestResponse);
                continue;
            }
            latestResponse = refundPayment(userId, payment.getId(), amount, "Order cancelled by customer");
            if ( latestResponse.getStatus() != RefundStatus.REFUNDED ) {
                allRefunded = false;
            }
        }
        if ( allRefunded && latestResponse != null ) {
            log.info("ORDER FULLY REFUNDED | orderId={} | latestRefundId={}", orderId, latestResponse.getId());
            return latestResponse;
        }
        if ( latestResponse != null ) {
            latestResponse.setStatus(RefundStatus.PENDING);
        }
        log.info("ORDER REFUND PENDING | orderId={}", orderId);
        return latestResponse;
    }

    @Override
    @Transactional
    public void handleWebhook( PaymentGateway gateway, String payload, String signature, String eventId ) {
        log.info("WEBHOOK RECEIVED | gateway={} | eventId={}", gateway, eventId);
        PaymentProvider provider = paymentProviderFactory.getProvider(gateway);
        PaymentWebhookEvent event = provider.parseWebhook(payload, signature, eventId);
        log.info("WEBHOOK PARSED | gateway={} | eventId={} | eventType={} | paymentStatus={} | refundStatus={}", gateway, event.getEventId(), event.getEventType(), event.getPaymentStatus(), event.getRefundStatus());
        if ( event.getEventId() == null ) {
            log.error("WEBHOOK EVENT ID MISSING | gateway={}", gateway);
            throw new PaymentException("Webhook event ID is missing");
        }
        boolean alreadyProcessed = processedWebhookEventRepository.existsByGatewayAndEventId(gateway.name(), event.getEventId());
        if ( alreadyProcessed ) {
            log.warn("WEBHOOK ALREADY PROCESSED | gateway={} | eventId={} | eventType={}", gateway, event.getEventId(), event.getEventType());
            return;
        }
        if ( event.getGatewayRefundId() != null && event.getRefundStatus() != null ) {
            log.info("PROCESSING REFUND WEBHOOK | gateway={} | eventId={} | gatewayRefundId={} | refundStatus={}", gateway, event.getEventId(), event.getGatewayRefundId(), event.getRefundStatus());
            handleRefundWebhook(event);
            saveProcessedWebhookEvent(event);
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
            log.debug("PAYMENT NOT FOUND BY GATEWAY PAYMENT ID | gateway={} | gatewayPaymentId={} | trying gatewayOrderId={}", gateway, event.getGatewayPaymentId(), event.getGatewayOrderId());
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
        log.debug("FETCHING PAYMENT | userId={} | paymentId={}", userId, paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found"));
        if ( !payment.getUserId()
                .equals(userId) ) {
            log.warn("PAYMENT ACCESS DENIED | userId={} | paymentUserId={} | paymentId={}", userId, payment.getUserId(), paymentId);
            throw new PaymentException("You are not allowed to access this payment");
        }
        return payment;
    }

    private void validateOrderForPayment( ProductOrderResponse order, UUID orderId ) {
        if ( order == null ) {
            log.warn("PAYMENT VALIDATION FAILED | orderId={} | reason=Order not found", orderId);
            throw new PaymentException("Order not found: " + orderId);
        }
        if ( order.getStatus() != OrderStatus.PAYMENT_PENDING ) {
            log.warn("PAYMENT VALIDATION FAILED | orderId={} | reason=Invalid order status | status={}", orderId, order.getStatus());
            throw new PaymentException("Order is not ready for payment. Current status: " + order.getStatus());
        }
        if ( order.getTotalAmount() == null || order.getTotalAmount()
                .compareTo(BigDecimal.ZERO) <= 0 ) {
            log.warn("PAYMENT VALIDATION FAILED | orderId={} | reason=Invalid amount | amount={}", orderId, order.getTotalAmount());
            throw new PaymentException("Order total amount must be greater than zero");
        }
        if ( order.getCurrency() == null ) {
            log.warn("PAYMENT VALIDATION FAILED | orderId={} | reason=Currency missing", orderId);
            throw new PaymentException("Order currency is required");
        }
        log.debug("ORDER PAYMENT VALIDATION PASSED | orderId={} | amount={} | currency={}", orderId, order.getTotalAmount(), order.getCurrency());
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

    @Transactional
    protected void handleSuccessfulPayment( Payment payment, PaymentWebhookEvent event ) {
        log.info("HANDLE SUCCESSFUL PAYMENT | paymentId={} | orderId={} | eventType={}", payment.getId(), payment.getOrderId(), event.getEventType());
        List<Payment> orderPayments = paymentRepository.lockAllByOrderId(payment.getOrderId());
        log.debug("LOCKED ORDER PAYMENTS | orderId={} | count={}", payment.getOrderId(), orderPayments.size());
        for ( Payment orderPayment : orderPayments ) {
            entityManager.refresh(orderPayment, LockModeType.PESSIMISTIC_WRITE);
        }
        if ( payment.getStatus() == PaymentStatus.SUCCESS ) {
            log.info("PAYMENT ALREADY SUCCESSFUL | paymentId={} | orderId={}", payment.getId(), payment.getOrderId());
            return;
        }
        if ( orderPayments.stream()
                .anyMatch(other -> !other.getId()
                        .equals(payment.getId()) && other.getStatus() == PaymentStatus.SUCCESS) ) {
            log.warn("DUPLICATE SUCCESSFUL PAYMENT DETECTED | paymentId={} | orderId={}", payment.getId(), payment.getOrderId());
            payment.setStatus(PaymentStatus.SUCCESS);
            if ( event.getGatewayPaymentId() != null ) {
                payment.setGatewayPaymentId(event.getGatewayPaymentId());
            }
            paymentRepository.save(payment);
            refundPayment(payment.getUserId(), payment.getId(), payment.getAmount()
                    .subtract(payment.getRefundedAmount()), "Duplicate successful payment for order");
            log.info("DUPLICATE PAYMENT REFUND INITIATED | paymentId={} | orderId={}", payment.getId(), payment.getOrderId());
            return;
        }
        ProductOrderResponse order = orderServiceClient.getOrderById(payment.getUserId(), payment.getOrderId());
        log.info("ORDER STATUS CHECKED BEFORE PAYMENT SUCCESS | orderId={} | status={}", payment.getOrderId(), order.getStatus());
        if ( order.getStatus() != OrderStatus.PAYMENT_PENDING ) {
            log.warn("PAYMENT ARRIVED AFTER ORDER WAS NO LONGER PAYABLE | paymentId={} | orderId={} | orderStatus={}", payment.getId(), payment.getOrderId(), order.getStatus());
            payment.setStatus(PaymentStatus.SUCCESS);
            if ( event.getGatewayPaymentId() != null ) {
                payment.setGatewayPaymentId(event.getGatewayPaymentId());
            }
            paymentRepository.save(payment);
            refundPayment(payment.getUserId(), payment.getId(), payment.getAmount()
                    .subtract(payment.getRefundedAmount()), "Payment succeeded after order was no longer payable");
            log.info("LATE PAYMENT REFUND INITIATED | paymentId={} | orderId={}", payment.getId(), payment.getOrderId());
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
        log.info("PAYMENT MARKED SUCCESS | paymentId={} | orderId={}", payment.getId(), payment.getOrderId());
        log.info("UPDATING ORDER TO CONFIRMED | orderId={}", payment.getOrderId());
        updateOrderStatus(payment.getUserId(), payment, OrderStatus.CONFIRMED, "Payment successful", "Payment successfully confirmed by payment gateway");
        log.info("ORDER CONFIRMED AFTER PAYMENT | orderId={} | paymentId={}", payment.getOrderId(), payment.getId());
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
    }

    private void saveProcessedWebhookEvent( PaymentWebhookEvent event ) {
        log.debug("SAVING PROCESSED WEBHOOK EVENT | gateway={} | eventId={} | eventType={}", event.getGateway(), event.getEventId(), event.getEventType());
        ProcessedWebhookEvent processedEvent = ProcessedWebhookEvent.builder()
                .gateway(event.getGateway()
                        .name())
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .processedAt(java.time.Instant.now())
                .build();
        processedWebhookEventRepository.save(processedEvent);
        log.debug("PROCESSED WEBHOOK EVENT SAVED | gateway={} | eventId={}", event.getGateway(), event.getEventId());
    }

    private void handleRefundWebhook( PaymentWebhookEvent event ) {
        log.info("HANDLE REFUND WEBHOOK | gateway={} | gatewayRefundId={} | refundStatus={}", event.getGateway(), event.getGatewayRefundId(), event.getRefundStatus());
        Refund refund = refundRepository.findByGatewayRefundId(event.getGatewayRefundId())
                .orElseThrow(() -> new PaymentException("Refund not found for webhook: " + event.getGatewayRefundId()));
        RefundStatus previousStatus = refund.getStatus();
        log.info("REFUND FOUND FOR WEBHOOK | refundId={} | paymentId={} | previousStatus={} | newStatus={}", refund.getId(), refund.getPayment()
                .getId(), previousStatus, event.getRefundStatus());
        refund.setStatus(event.getRefundStatus());
        refund.setFailureReason(event.getRefundStatus() == RefundStatus.FAILED ? event.getFailureReason() : null);
        refundRepository.save(refund);
        Payment payment = refund.getPayment();
        if ( event.getRefundStatus() == RefundStatus.REFUNDED && previousStatus != RefundStatus.REFUNDED ) {
            payment.setRefundedAmount(payment.getRefundedAmount()
                    .add(refund.getAmount()));
            payment.setStatus(payment.getRefundedAmount()
                    .compareTo(payment.getAmount()) >= 0 ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
            paymentRepository.save(payment);
            log.info("PAYMENT REFUND AMOUNT UPDATED FROM WEBHOOK | paymentId={} | refundedAmount={} | status={}", payment.getId(), payment.getRefundedAmount(), payment.getStatus());
            ProductOrderResponse order = orderServiceClient.getOrderById(payment.getUserId(), payment.getOrderId());
            if ( order.getStatus() == OrderStatus.REFUND_PENDING ) {
                log.info("UPDATING ORDER TO REFUNDED | orderId={} | paymentId={}", payment.getOrderId(), payment.getId());
                updateOrderStatus(payment.getUserId(), payment, OrderStatus.REFUNDED, "Refund completed", "Order payment was refunded successfully");
            }
        }
    }
}