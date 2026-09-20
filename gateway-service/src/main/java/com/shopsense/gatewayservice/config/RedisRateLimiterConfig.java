package com.shopsense.gatewayservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RedisRateLimiterConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiterConfig.class);

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(20, 40, 1);
    }

    @Bean
    public KeyResolver userOrIpKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-User-Id");
            if ( userId != null && !userId.isBlank() ) {
                String key = "user:" + userId;
                log.info("Rate limit key resolved from user ID: {}", key);
                return Mono.just(key);
            }
            if ( exchange.getRequest()
                    .getRemoteAddress() == null || exchange.getRequest()
                    .getRemoteAddress()
                    .getAddress() == null ) {
                log.info("Rate limit key resolved: unknown client");
                return Mono.just("unknown");
            }
            String ipAddress = exchange.getRequest()
                    .getRemoteAddress()
                    .getAddress()
                    .getHostAddress();
            String key = "ip:" + ipAddress;
            log.info("Rate limit key resolved from IP address: {}", key);
            return Mono.just(key);
        };
    }
}