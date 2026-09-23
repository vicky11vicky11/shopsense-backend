package com.shopsense.paymentservice.config;

import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RazorpayClientConfig {

    private final RazorpayProperties properties;

    @Bean
    public RazorpayClient razorpayClient() throws Exception {

        return new RazorpayClient(properties.getKeyId(), properties.getKeySecret());
    }
}