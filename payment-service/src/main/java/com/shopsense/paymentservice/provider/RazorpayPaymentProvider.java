package com.shopsense.paymentservice.provider;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.razorpay.Utils;
import com.shopsense.paymentservice.config.RazorpayProperties;
import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.enums.PaymentStatus;
import com.shopsense.paymentservice.enums.RefundStatus;
import com.shopsense.paymentservice.exceptions.WebhookVerificationException;
import com.shopsense.paymentservice.request.CreateGatewayPaymentRequest;
import com.shopsense.paymentservice.request.GatewayRefundRequest;
import com.shopsense.paymentservice.request.PaymentWebhookEvent;
import com.shopsense.paymentservice.request.VerifyPaymentRequest;
import com.shopsense.paymentservice.response.GatewayPaymentResponse;
import com.shopsense.paymentservice.response.GatewayRefundResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class RazorpayPaymentProvider implements PaymentProvider {

    private final RazorpayClient razorpayClient;

    private final RazorpayProperties razorpayProperties;

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.RAZORPAY;
    }

    @Override
    public GatewayPaymentResponse createPayment( CreateGatewayPaymentRequest request ) {
        try {
            JSONObject orderRequest = getOrderRequest(request);
            Order order = razorpayClient.orders.create(orderRequest);
            return GatewayPaymentResponse.builder()
                    .gatewayOrderId(order.get("id"))
                    .checkoutKey(razorpayProperties.getKeyId())
                    .status(order.get("status"))
                    .build();
        } catch ( Exception exception ) {
            throw new RuntimeException("Failed to create Razorpay order", exception);
        }
    }

    @Override
    public boolean verifyPayment( VerifyPaymentRequest request ) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getGatewayOrderId());
            attributes.put("razorpay_payment_id", request.getGatewayPaymentId());
            attributes.put("razorpay_signature", request.getSignature());
            Utils.verifyPaymentSignature(attributes, razorpayProperties.getKeySecret());
            return true;
        } catch ( Exception exception ) {
            return false;
        }
    }

    @Override
    public GatewayRefundResponse refund( GatewayRefundRequest request ) {
        try {
            JSONObject refundRequest = getRefundRequest(request);
            Refund refund = razorpayClient.payments.refund(request.getGatewayPaymentId(), refundRequest);
            return GatewayRefundResponse.builder()
                    .gatewayRefundId(refund.get("id"))
                    .status(refund.get("status"))
                    .build();
        } catch ( Exception exception ) {
            throw new RuntimeException("Failed to create Razorpay refund", exception);
        }
    }

    @Override
    public PaymentWebhookEvent parseWebhook( String payload, String signature, String eventId ) {
        try {
            Utils.verifyWebhookSignature(payload, signature, razorpayProperties.getWebhookSecret());
            JSONObject root = new JSONObject(payload);
            String eventType = root.optString("event", null);
            if ( eventType == null ) {
                throw new WebhookVerificationException("Razorpay webhook event type is missing");
            }
            JSONObject payloadObject = root.optJSONObject("payload");
            if ( payloadObject == null ) {
                throw new WebhookVerificationException("Razorpay webhook payload is missing");
            }
            String stableEventId = eventId != null && !eventId.isBlank() ? eventId : fallbackEventId(payload);
            JSONObject refundObject = extractRefundEntity(payloadObject);
            if ( refundObject != null ) {
                return PaymentWebhookEvent.builder()
                        .eventId(stableEventId)
                        .gateway(PaymentGateway.RAZORPAY)
                        .eventType(eventType)
                        .gatewayRefundId(refundObject.optString("id", null))
                        .gatewayPaymentId(refundObject.optString("payment_id", null))
                        .refundStatus(mapRefundStatus(eventType, refundObject.optString("status", null)))
                        .failureReason(refundObject.optString("error_description", null))
                        .build();
            }
            JSONObject paymentObject = extractPaymentEntity(payloadObject);
            if ( paymentObject == null ) {
                return PaymentWebhookEvent.builder()
                        .eventId(stableEventId)
                        .gateway(PaymentGateway.RAZORPAY)
                        .eventType(eventType)
                        .build();
            }
            String gatewayPaymentId = paymentObject.optString("id", null);
            String gatewayOrderId = paymentObject.optString("order_id", null);
            String paymentMethod = paymentObject.optString("method", null);
            PaymentStatus status = mapPaymentStatus(eventType);
            String failureReason = null;
            if ( status == PaymentStatus.FAILED ) {
                failureReason = paymentObject.optString("error_description", null);
            }
            return PaymentWebhookEvent.builder()
                    .eventId(stableEventId)
                    .gateway(PaymentGateway.RAZORPAY)
                    .eventType(eventType)
                    .gatewayPaymentId(gatewayPaymentId)
                    .gatewayOrderId(gatewayOrderId)
                    .paymentMethod(paymentMethod)
                    .paymentStatus(status)
                    .failureReason(failureReason)
                    .build();
        } catch ( WebhookVerificationException exception ) {
            throw exception;
        } catch ( Exception exception ) {
            throw new WebhookVerificationException("Invalid Razorpay webhook");
        }
    }


    private JSONObject extractPaymentEntity( JSONObject payloadObject ) {
        JSONObject payment = payloadObject.optJSONObject("payment");
        if ( payment == null ) {
            return null;
        }
        return payment.optJSONObject("entity");
    }

    private JSONObject extractRefundEntity( JSONObject payloadObject ) {
        JSONObject refund = payloadObject.optJSONObject("refund");
        return refund == null ? null : refund.optJSONObject("entity");
    }

    private String fallbackEventId( String payload ) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8));
        return "payload-" + HexFormat.of().formatHex(digest);
    }

    private RefundStatus mapRefundStatus( String eventType, String gatewayStatus ) {
        if ( "refund.processed".equals(eventType) || "processed".equalsIgnoreCase(gatewayStatus) ) return RefundStatus.REFUNDED;
        if ( "refund.failed".equals(eventType) || "failed".equalsIgnoreCase(gatewayStatus) ) return RefundStatus.FAILED;
        return RefundStatus.PENDING;
    }


    private PaymentStatus mapPaymentStatus( String eventType ) {
        switch ( eventType ) {
            case "payment.captured":
            case "payment.authorized":
            case "order.paid":
                return PaymentStatus.SUCCESS;
            case "payment.failed":
                return PaymentStatus.FAILED;
            default:
                return null;
        }
    }

    private static @NonNull JSONObject getOrderRequest( CreateGatewayPaymentRequest request ) {
        long amountInPaise = request.getAmount()
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", request.getCurrency()
                .name());
        orderRequest.put("receipt", request.getOrderId()
                .toString());
        JSONObject notes = new JSONObject();
        notes.put("order_id", request.getOrderId()
                .toString());
        notes.put("user_id", request.getUserId());
        orderRequest.put("notes", notes);
        return orderRequest;
    }

    private static @NonNull JSONObject getRefundRequest( GatewayRefundRequest request ) {
        long amountInPaise = request.getAmount()
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("amount", amountInPaise);
        if ( request.getReason() != null ) {
            JSONObject notes = new JSONObject();
            notes.put("reason", request.getReason());
            refundRequest.put("notes", notes);
        }
        return refundRequest;
    }

}
