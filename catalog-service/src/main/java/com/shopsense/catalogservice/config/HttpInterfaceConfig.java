package com.shopsense.catalogservice.config;

import com.shopsense.catalogservice.client.MediaServiceClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpInterfaceConfig {

    @Bean
    public MediaServiceClient mediaServiceClient( WebClient.Builder webClientBuilder, ReactorLoadBalancerExchangeFilterFunction reactorLoadBalancerExchangeFilterFunction ) {
        WebClient webClient = webClientBuilder.baseUrl("http://media-service")
                .filter(reactorLoadBalancerExchangeFilterFunction)
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(MediaServiceClient.class);
    }
}
