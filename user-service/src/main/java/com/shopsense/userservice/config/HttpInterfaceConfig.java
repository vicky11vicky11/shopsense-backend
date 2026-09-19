package com.shopsense.userservice.config;

import com.shopsense.userservice.client.MediaServiceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpInterfaceConfig {

    @Bean
    public MediaServiceClient mediaServiceClient( @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder ) {
        WebClient webClient = webClientBuilder.baseUrl("http://media-service")
                .build();
        WebClientAdapter webClientAdapter = WebClientAdapter.create(webClient);
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(webClientAdapter)
                .build();
        return httpServiceProxyFactory.createClient(MediaServiceClient.class);
    }

}
