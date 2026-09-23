package com.shopsense.paymentservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StripeClientConfig {

    private final StripeProperties properties;

    @PostConstruct
    public void initialize() {
        Stripe.apiKey = properties.getSecretKey();
    }
}