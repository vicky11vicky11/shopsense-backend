package com.shopsense.inventoryservice.filter;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class HttpTraceCaptureFilter extends OncePerRequestFilter {

    private static final int MAX_BODY_LENGTH = 4_000;

    private static final int REQUEST_CACHE_LIMIT = 4 * 1024;

    private final Tracer tracer;

    @Override
    protected void doFilterInternal( @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, FilterChain filterChain ) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            Span span = tracer.currentSpan();
            if ( span != null ) {
                span.tag("http.request.method", request.getMethod());
                span.tag("http.route", getRoute(request));
                span.tag("http.response.status_code", String.valueOf(response.getStatus()));
                captureRequestHeaders(requestWrapper, span);
                captureResponseHeaders(responseWrapper, span);
                String requestBody = getRequestBody(requestWrapper);
                if ( !requestBody.isBlank() ) {
                    span.tag("http.request.body", truncate(requestBody));
                }
                String responseBody = getResponseBody(responseWrapper);
                if ( !responseBody.isBlank() ) {
                    span.tag("http.response.body", truncate(responseBody));
                }
            }
            responseWrapper.copyBodyToResponse();
        }
    }

    private void captureRequestHeaders( HttpServletRequest request, Span span ) {
        captureHeader(request, span, "Content-Type");
        captureHeader(request, span, "Accept");
        captureHeader(request, span, "X-Request-ID");
    }

    private void captureResponseHeaders( HttpServletResponse response, Span span ) {
        captureResponseHeader(response, span, "Content-Type");
        captureResponseHeader(response, span, "Content-Length");
    }

    private void captureHeader( HttpServletRequest request, Span span, String headerName ) {
        String value = request.getHeader(headerName);
        if ( value != null ) {
            span.tag("http.request.header." + headerName.toLowerCase(), value);
        }
    }

    private void captureResponseHeader( HttpServletResponse response, Span span, String headerName ) {
        String value = response.getHeader(headerName);
        if ( value != null ) {
            span.tag("http.response.header." + headerName.toLowerCase(), value);
        }
    }

    private String getRequestBody( ContentCachingRequestWrapper request ) {
        byte[] content = request.getContentAsByteArray();
        if ( content.length == 0 ) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private String getResponseBody( ContentCachingResponseWrapper response ) {
        byte[] content = response.getContentAsByteArray();
        if ( content.length == 0 ) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private String truncate( String value ) {
        if ( value.length() <= MAX_BODY_LENGTH ) {
            return value;
        }
        return value.substring(0, MAX_BODY_LENGTH) + "...[truncated]";
    }

    private String getRoute( HttpServletRequest request ) {
        String pattern = (String) request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        return pattern != null ? pattern : request.getRequestURI();
    }
}