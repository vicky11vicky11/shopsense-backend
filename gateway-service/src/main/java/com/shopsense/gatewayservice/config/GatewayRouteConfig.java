package com.shopsense.gatewayservice.config;

import com.shopsense.gatewayservice.utils.ServiceName;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator gatewayRoutes( RouteLocatorBuilder builder, RedisRateLimiter redisRateLimiter, KeyResolver userOrIpKeyResolver ) {

        return builder.routes()

                .route("catalog-service", route -> route.path("/api/v1/products/**", "/api/v1/categories/**", "/api/v1/brands/**", "/api/v1/stores/**")
                        .filters(filters -> filters.requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter)
                                .setKeyResolver(userOrIpKeyResolver)))
                        .uri(ServiceName.CATALOG))

                .route("media-service", route -> route.path("/api/v1/media/**")
                        .filters(filters -> filters.requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter)
                                .setKeyResolver(userOrIpKeyResolver)))
                        .uri(ServiceName.MEDIA))

                .route("order-service", route -> route.path("/api/v1/cart/**", "/api/v1/orders/**")
                        .filters(filters -> filters.requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter)
                                .setKeyResolver(userOrIpKeyResolver)))
                        .uri(ServiceName.ORDER))

                .route("user-service", route -> route.path("/api/v1/customers/**", "/api/v1/sellers/**", "/api/v1/admins/**", "/api/v1/addresses/**")
                        .filters(filters -> filters.requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter)
                                .setKeyResolver(userOrIpKeyResolver)))
                        .uri(ServiceName.USER))

                .route("inventory-service", route -> route.path("/api/v1/inventory/**", "/api/v1/reservations/**")
                        .filters(filters -> filters.requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter)
                                .setKeyResolver(userOrIpKeyResolver)))
                        .uri(ServiceName.INVENTORY))

                .build();
    }
}