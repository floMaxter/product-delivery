package com.productdelivery.customerservice.config;

import com.productdelivery.customerservice.client.WebClientFavouriteProductClient;
import com.productdelivery.customerservice.client.WebClientProductReviewsClient;
import com.productdelivery.customerservice.client.WebClientProductsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    @Bean
    @Scope("prototype")
    public WebClient.Builder productDeliveryServicesWebClientBuilder(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                        authorizedClientRepository);
        filter.setDefaultClientRegistrationId("keycloak");
        return WebClient.builder()
                .filter(filter);
    }

    @Bean
    public WebClientProductsClient webClientProductsClient(
            @Value("${productdelivery.services.catalog.uri:http://localhost:8081}") String catalogBaseUrl,
            WebClient.Builder productDeliveryServicesWebClientBuilder) {
        return new WebClientProductsClient(productDeliveryServicesWebClientBuilder
                .baseUrl(catalogBaseUrl)
                .build());
    }

    @Bean
    public WebClientFavouriteProductClient webClientFavouriteProductsClient(
            @Value("${productdelivery.services.feedback.uri:http://localhost:8084}") String feedbackBaseUrl,
            WebClient.Builder productDeliveryServicesWebClientBuilder) {
        return new WebClientFavouriteProductClient(productDeliveryServicesWebClientBuilder
                .baseUrl(feedbackBaseUrl)
                .build());
    }

    @Bean
    public WebClientProductReviewsClient webClientProductReviewsClient(
            @Value("${productdelivery.services.feedback.uri:http://localhost:8084}") String feedbackBaseUrl,
            WebClient.Builder productDeliveryServicesWebClientBuilder) {
        return new WebClientProductReviewsClient(productDeliveryServicesWebClientBuilder
                .baseUrl(feedbackBaseUrl)
                .build());
    }
}