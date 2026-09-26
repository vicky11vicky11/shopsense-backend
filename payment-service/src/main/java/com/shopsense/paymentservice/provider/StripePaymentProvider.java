package com.shopsense.paymentservice.provider;

import com.shopsense.paymentservice.config.StripeProperties;
import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.enums.PaymentStatus;
import com.shopsense.paymentservice.enums.RefundStatus;
import com.shopsense.paymentservice.exceptions.GatewayException;
import com.shopsense.paymentservice.exceptions.WebhookVerificationException;
import com.shopsense.paymentservice.request.CreateGatewayPaymentRequest;
import com.shopsense.paymentservice.request.GatewayRefundRequest;
import com.shopsense.paymentservice.request.PaymentWebhookEvent;
import com.shopsense.paymentservice.request.VerifyPaymentRequest;
import com.shopsense.paymentservice.response.GatewayPaymentResponse;
import com.shopsense.paymentservice.response.GatewayRefundResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripePaymentProvider implements PaymentProvider {

    private final StripeProperties stripeProperties;

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.STRIPE;
    }

    @Override
    public GatewayPaymentResponse createPayment( CreateGatewayPaymentRequest request ) {
        try {
            long amount = request.getAmount()
                    .movePointRight(2)
                    .setScale(0, java.math.RoundingMode.HALF_UP)
                    .longValueExact();
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(request.getCurrency()
                            .name()
                            .toLowerCase())
                    .setDescription(request.getDescription())
                    .putMetadata("order_id", request.getOrderId()
                            .toString())
                    .putMetadata("user_id", request.getUserId())
                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build())
                    .build();
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return GatewayPaymentResponse.builder()
                    .gatewayPaymentId(paymentIntent.getId())
                    .checkoutKey(stripeProperties.getPublishableKey())
                    .checkoutClientSecret(paymentIntent.getClientSecret())
                    .status(paymentIntent.getStatus())
                    .build();
        } catch ( StripeException exception ) {
            throw new GatewayException("Failed to create Stripe payment: " + exception.getMessage());
        }
    }

    @Override
    public boolean verifyPayment( VerifyPaymentRequest request ) {
        return false;
    }

    @Override
    public GatewayRefundResponse refund( GatewayRefundRequest request ) {
        try {
            long amount = request.getAmount()
                    .movePointRight(2)
                    .setScale(0, java.math.RoundingMode.HALF_UP)
                    .longValueExact();
            RefundCreateParams.Builder builder = RefundCreateParams.builder()
                    .setPaymentIntent(request.getGatewayPaymentId())
                    .setAmount(amount);
            RefundCreateParams params = builder.build();
            Refund refund = Refund.create(params);
            return GatewayRefundResponse.builder()
                    .gatewayRefundId(refund.getId())
                    .status(refund.getStatus())
                    .build();
        } catch ( StripeException exception ) {
            throw new GatewayException("Stripe refund failed: " + exception.getMessage());
        }
    }

    @Override
    public PaymentWebhookEvent parseWebhook( String payload, String signature, String eventId ) {
        try {
            Event event = Webhook.constructEvent(payload, signature, stripeProperties.getWebhookSecret());
            String actualEventId = event.getId();
            String eventType = event.getType();
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            Object stripeObject = deserializer.getObject()
                    .orElse(null);
            if ( stripeObject instanceof Refund stripeRefund ) {
                RefundStatus refundStatus = switch (stripeRefund.getStatus() == null ? "" : stripeRefund.getStatus().toLowerCase()) {
                    case "succeeded" -> RefundStatus.REFUNDED;
                    case "failed", "canceled" -> RefundStatus.FAILED;
                    default -> RefundStatus.PENDING;
                };
                return PaymentWebhookEvent.builder()
                        .eventId(actualEventId)
                        .gateway(PaymentGateway.STRIPE)
                        .eventType(eventType)
                        .gatewayRefundId(stripeRefund.getId())
                        .gatewayPaymentId(stripeRefund.getPaymentIntent())
                        .refundStatus(refundStatus)
                        .failureReason(stripeRefund.getFailureReason())
                        .build();
            }
            if ( !( stripeObject instanceof PaymentIntent ) ) {
                return PaymentWebhookEvent.builder()
                        .eventId(actualEventId)
                        .gateway(PaymentGateway.STRIPE)
                        .eventType(eventType)
                        .build();
            }
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            String gatewayPaymentId = paymentIntent.getId();
            String gatewayOrderId = null;
            if ( paymentIntent.getMetadata() != null && paymentIntent.getMetadata()
                    .containsKey("order_id") ) {
                gatewayOrderId = paymentIntent.getMetadata()
                        .get("order_id");
            }
            PaymentStatus status = mapPaymentStatus(eventType);
            String failureReason = null;
            if ( status == PaymentStatus.FAILED && paymentIntent.getLastPaymentError() != null ) {
                failureReason = paymentIntent.getLastPaymentError()
                        .getMessage();
            }
            return PaymentWebhookEvent.builder()
                    .eventId(actualEventId)
                    .gateway(PaymentGateway.STRIPE)
                    .eventType(eventType)
                    .gatewayPaymentId(gatewayPaymentId)
                    .gatewayOrderId(gatewayOrderId)
                    .paymentStatus(status)
                    .failureReason(failureReason)
                    .build();
        } catch ( Exception exception ) {
            throw new WebhookVerificationException("Invalid Stripe webhook");
        }
    }


    private PaymentStatus mapPaymentStatus( String eventType ) {
        return switch ( eventType ) {
            case "payment_intent.succeeded" -> PaymentStatus.SUCCESS;
            case "payment_intent.payment_failed" -> PaymentStatus.FAILED;
            default -> null;
        };
    }
}
