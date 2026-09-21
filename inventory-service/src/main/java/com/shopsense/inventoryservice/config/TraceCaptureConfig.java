package com.shopsense.inventoryservice.config;

import com.shopsense.inventoryservice.filter.HttpTraceCaptureFilter;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TraceCaptureConfig {

    @Bean
    public FilterRegistrationBean<HttpTraceCaptureFilter> httpTraceCaptureFilter( Tracer tracer ) {
        FilterRegistrationBean<HttpTraceCaptureFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new HttpTraceCaptureFilter(tracer));
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MAX_VALUE);
        return registration;
    }
}