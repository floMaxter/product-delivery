package com.productdelivery.customerservice.config;

import com.productdelivery.customerservice.client.WebClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    public WebClientProductsClient webClientProductsClient(
            @Value("${productdelivery.services.catalog.uri:http://localhost:8081}") String catalogBaseUrl
    ) {
        return new  WebClientProductsClient(WebClient.builder()
                .baseUrl(catalogBaseUrl)
                .build());

    }
}
