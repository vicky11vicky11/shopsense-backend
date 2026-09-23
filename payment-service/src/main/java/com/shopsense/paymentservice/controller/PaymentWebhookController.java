package com.shopsense.paymentservice.controller;

import com.shopsense.paymentservice.enums.PaymentGateway;
import com.shopsense.paymentservice.service.PaymentService;
import com.shopsense.paymentservice.utils.AppUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(AppUrl.PAYMENTS_URL)
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhooks/razorpay")
    public ResponseEntity<Void> razorpayWebhook( @RequestHeader(value = "X-Razorpay-Signature") String signature, @RequestHeader(value = "x-razorpay-event-id", required = false) String eventId, @RequestBody String payload ) {
        log.info("RAZORPAY WEBHOOK RECEIVED | eventId={} | payloadLength={}", eventId, payload != null ? payload.length() : 0);
        paymentService.handleWebhook(PaymentGateway.RAZORPAY, payload, signature, eventId);
        log.info("RAZORPAY WEBHOOK PROCESSED SUCCESSFULLY | eventId={}", eventId);
        return ResponseEntity.ok()
                .build();
    }


    @PostMapping("/webhooks/stripe")
    public ResponseEntity<Void> stripeWebhook( @RequestHeader("Stripe-Signature") String signature, @RequestBody String payload ) {
        log.info("STRIPE WEBHOOK RECEIVED | payloadLength={}", payload != null ? payload.length() : 0);
        paymentService.handleWebhook(PaymentGateway.STRIPE, payload, signature, null);
        log.info("STRIPE WEBHOOK PROCESSED SUCCESSFULLY");
        return ResponseEntity.ok()
                .build();
    }
}