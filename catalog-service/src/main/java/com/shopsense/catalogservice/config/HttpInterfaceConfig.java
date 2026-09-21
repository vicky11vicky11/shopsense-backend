package com.shopsense.catalogservice.config;

import com.shopsense.catalogservice.client.AddressServiceClient;
import com.shopsense.catalogservice.client.MediaServiceClient;
import com.shopsense.catalogservice.client.SellerServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public MediaServiceClient mediaServiceClient( WebClient.Builder webClientBuilder ) {
        return createClient(webClientBuilder, "http://media-service", MediaServiceClient.class);
    }

    @Bean
    public AddressServiceClient addressServiceClient( WebClient.Builder webClientBuilder ) {
        return createClient(webClientBuilder, "http://user-service", AddressServiceClient.class);
    }

    @Bean
    public SellerServiceClient sellerServiceClient( WebClient.Builder webClientBuilder ) {
        return createClient(webClientBuilder, "http://user-service", SellerServiceClient.class);
    }

    private <T> T createClient( WebClient.Builder builder, String baseUrl, Class<T> clientType ) {
        WebClient webClient = builder.clone()
                .baseUrl(baseUrl)
                .filter(loadBalancerFilter)
                .build();
        WebClientAdapter adapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter)
                .build();
        return factory.createClient(clientType);
    }

}
