package com.productdelivery.managerservice.config;

import com.productdelivery.managerservice.client.RestClientProductsRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientBeans {

    @Bean
    public RestClientProductsRestClient productsRestClient(
            @Value("${productdelivery.services.catalog.uri:http://localhost:8081}") String catalogBaseUri,
            @Value("${productdelivery.services.catalog.username:}") String catalogUsername,
            @Value("${productdelivery.services.catalog.password:}") String catalogPassword) {
        return new RestClientProductsRestClient(RestClient.builder()
                .baseUrl(catalogBaseUri)
                .requestInterceptor(
                        new BasicAuthenticationInterceptor(catalogUsername, catalogPassword))
                .build());
    }
}
