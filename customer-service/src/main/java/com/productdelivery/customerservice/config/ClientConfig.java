package com.productdelivery.customerservice.config;

import com.productdelivery.customerservice.client.WebClientFavouriteProductClient;
import com.productdelivery.customerservice.client.WebClientProductReviewsClient;
import com.productdelivery.customerservice.client.WebClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    public WebClientProductsClient webClientProductsClient(
            @Value("${productdelivery.services.catalog.uri:http://localhost:8081}") String catalogBaseUrl) {
        return new WebClientProductsClient(WebClient.builder()
                .baseUrl(catalogBaseUrl)
                .build());
    }

    @Bean
    public WebClientFavouriteProductClient webClientFavouriteProductsClient(
            @Value("${productdelivery.services.feedback.uri:http://localhost:8084}") String feedbackBaseUrl) {
        return new WebClientFavouriteProductClient(WebClient.builder()
                .baseUrl(feedbackBaseUrl)
                .build());
    }

    @Bean
    public WebClientProductReviewsClient webClientProductReviewsClient(
            @Value("${productdelivery.services.feedback.uri:http://localhost:8084}") String feedbackBaseUrl) {
        return new WebClientProductReviewsClient(WebClient.builder()
                .baseUrl(feedbackBaseUrl)
                .build());
    }
}
