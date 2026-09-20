package com.shopsense.gatewayservice.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler( ObjectMapper objectMapper ) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle( ServerWebExchange exchange, Throwable ex ) {
        log.error("Gateway exception for {} {}", exchange.getRequest()
                .getMethod(), exchange.getRequest()
                .getURI(), ex);
        HttpStatus status = resolveStatus(ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, resolveDetail(ex));
        problemDetail.setTitle(resolveTitle(status));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", exchange.getRequest()
                .getPath()
                .value());
        return writeResponse(exchange, problemDetail);
    }

    private HttpStatus resolveStatus( Throwable ex ) {
        if ( ex instanceof NotFoundException ) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if ( ex instanceof GatewayServiceUnavailableException ) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        if ( ex instanceof GatewayTimeoutException ) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveTitle( HttpStatus status ) {
        return switch ( status ) {
            case SERVICE_UNAVAILABLE -> "Service Unavailable";
            case GATEWAY_TIMEOUT -> "Gateway Timeout";
            default -> "Internal Server Error";
        };
    }

    private String resolveDetail( Throwable ex ) {
        if ( ex instanceof NotFoundException ) {
            return ex.getMessage();
        }
        if ( ex instanceof GatewayServiceUnavailableException ) {
            return ex.getMessage();
        }
        if ( ex instanceof GatewayTimeoutException ) {
            return ex.getMessage();
        }
        return "An unexpected error occurred";
    }

    private Mono<Void> writeResponse( ServerWebExchange exchange, ProblemDetail problemDetail ) {
        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(problemDetail);
            DataBuffer buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(responseBytes);
            exchange.getResponse()
                    .setStatusCode(HttpStatus.valueOf(problemDetail.getStatus()));
            exchange.getResponse()
                    .getHeaders()
                    .setContentType(MediaType.APPLICATION_PROBLEM_JSON);
            return exchange.getResponse()
                    .writeWith(Mono.just(buffer));
        } catch ( Exception ex ) {
            log.error("Failed to create gateway error response", ex);
            return Mono.error(ex);
        }
    }
}