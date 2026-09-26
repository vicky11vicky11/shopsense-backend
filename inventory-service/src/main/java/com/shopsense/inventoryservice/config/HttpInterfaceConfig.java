package com.shopsense.inventoryservice.config;

import com.shopsense.inventoryservice.client.ProductServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
public class HttpInterfaceConfig {

    private final ReactorLoadBalancerExchangeFilterFunction loadBalancerFilter;

    @Bean
    public ProductServiceClient productServiceClient( WebClient.Builder webClientBuilder ) {
        WebClient webClient = webClientBuilder.clone()
                .baseUrl("http://catalog-service")
                .filter(loadBalancerFilter)
                .build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter)
                .build();
        return factory.createClient(ProductServiceClient.class);
    }
}