package com.shopsense.orderservice.config;

import com.shopsense.orderservice.client.*;
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
    public AddressServiceClient addressServiceClient( @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder ) {
        WebClient webClient = webClientBuilder.baseUrl("http://user-service")
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(AddressServiceClient.class);
    }

    @Bean
    public CustomerServiceClient customerServiceClient( @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder ) {
        WebClient webClient = webClientBuilder.baseUrl("http://user-service")
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(CustomerServiceClient.class);
    }

    @Bean
    public MediaServiceClient mediaServiceClient( @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder ) {
        WebClient webClient = webClientBuilder.baseUrl("http://media-service")
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(MediaServiceClient.class);
    }

    @Bean
    public InventoryServiceClient inventoryServiceClient( @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder ) {
        WebClient webClient = webClientBuilder.baseUrl("http://inventory-service")
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(InventoryServiceClient.class);
    }

    @Bean
    public StockReservationServiceClient stockReservationServiceClient( @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder ) {
        WebClient webClient = webClientBuilder.baseUrl("http://inventory-service")
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(StockReservationServiceClient.class);
    }

}
