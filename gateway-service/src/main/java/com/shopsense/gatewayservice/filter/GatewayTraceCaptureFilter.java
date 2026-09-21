package com.shopsense.gatewayservice.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GatewayTraceCaptureFilter implements GlobalFilter, Ordered {

    private final Tracer tracer;

    @Override
    public Mono<Void> filter( @NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain ) {
        Span span = tracer.currentSpan();
        if ( span != null ) {
            var request = exchange.getRequest();
            span.tag("http.request.method", request.getMethod()
                    .name());
            span.tag("http.route", request.getPath()
                    .value());
            String contentType = request.getHeaders()
                    .getFirst("Content-Type");
            if ( contentType != null ) {
                span.tag("http.request.header.content-type", contentType);
            }
            String accept = request.getHeaders()
                    .getFirst("Accept");
            if ( accept != null ) {
                span.tag("http.request.header.accept", accept);
            }
            String requestId = request.getHeaders()
                    .getFirst("X-Request-ID");
            if ( requestId != null ) {
                span.tag("http.request.header.x-request-id", requestId);
            }
        }
        return chain.filter(exchange)
                .doOnSuccess(unused -> {
                    Span currentSpan = tracer.currentSpan();
                    if ( currentSpan != null ) {
                        currentSpan.tag("http.response.status_code", String.valueOf(Objects.requireNonNull(exchange.getResponse()
                                        .getStatusCode())
                                .value()));
                        String contentType = exchange.getResponse()
                                .getHeaders()
                                .getFirst("Content-Type");
                        if ( contentType != null ) {
                            currentSpan.tag("http.response.header.content-type", contentType);
                        }
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}