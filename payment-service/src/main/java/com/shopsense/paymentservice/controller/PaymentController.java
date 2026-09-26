package com.shopsense.paymentservice.controller;

import com.razorpay.RazorpayException;
import com.shopsense.paymentservice.request.CreatePaymentRequest;
import com.shopsense.paymentservice.request.RefundRequest;
import com.shopsense.paymentservice.request.VerifyPaymentRequest;
import com.shopsense.paymentservice.response.PaymentResponse;
import com.shopsense.paymentservice.response.RefundResponse;
import com.shopsense.paymentservice.service.PaymentService;
import com.shopsense.paymentservice.utils.AppUrl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppUrl.PAYMENTS_URL)
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment( @Valid @RequestBody CreatePaymentRequest request, @RequestHeader("X-User-Id") String userId ) throws RazorpayException {
        PaymentResponse paymentResponse = paymentService.createPayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentResponse);
    }

    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<PaymentResponse> verifyRazorpayPayment( @RequestHeader("X-User-Id") String userId, @PathVariable UUID paymentId, @Valid @RequestBody VerifyPaymentRequest request ) {
        PaymentResponse paymentResponse = paymentService.verifyRazorpayPayment(userId, paymentId, request.getGatewayOrderId(), request.getGatewayPaymentId(), request.getSignature());
        return ResponseEntity.ok(paymentResponse);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment( @RequestHeader("X-User-Id") String userId, @PathVariable UUID paymentId ) {
        return ResponseEntity.ok(paymentService.getPayment(userId, paymentId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId( @RequestHeader("X-User-Id") String userId, @PathVariable UUID orderId ) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(userId, orderId));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<RefundResponse> refundPayment( @RequestHeader("X-User-Id") String userId, @PathVariable UUID paymentId, @Valid @RequestBody RefundRequest request ) {
        RefundResponse refundResponse = paymentService.refundPayment(userId, paymentId, request.getAmount(), request.getReason());
        return ResponseEntity.ok(refundResponse);
    }

    @PostMapping("/order/{orderId}/refund")
    public ResponseEntity<RefundResponse> refundOrder( @RequestHeader("X-User-Id") String userId, @PathVariable UUID orderId ) {
        return ResponseEntity.ok(paymentService.refundOrder(userId, orderId));
    }

}
