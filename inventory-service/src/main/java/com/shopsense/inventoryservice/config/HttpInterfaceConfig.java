package com.shopsense.inventoryservice.config;

import com.shopsense.inventoryservice.client.ProductServiceClient;
import com.shopsense.inventoryservice.client.OrderServiceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpInterfaceConfig {

    @Bean
    public ProductServiceClient productServiceClient( @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder ) {
        WebClient webClient = webClientBuilder.baseUrl("http://catalog-service")
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(ProductServiceClient.class);
    }

    @Bean
    public OrderServiceClient orderServiceClient( @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder ) {
        WebClient webClient = webClientBuilder.baseUrl("http://order-service")
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(OrderServiceClient.class);
    }
}
